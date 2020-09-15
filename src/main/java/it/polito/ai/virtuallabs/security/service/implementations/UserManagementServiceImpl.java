package it.polito.ai.virtuallabs.security.service.implementations;

import it.polito.ai.virtuallabs.security.dtos.UserDTO;
import it.polito.ai.virtuallabs.security.entities.Roles;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.security.repositories.UserRepository;
import it.polito.ai.virtuallabs.security.service.UserManagementService;
import it.polito.ai.virtuallabs.security.service.exceptions.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDTO createUser(UserDTO user) {
        String[] idDomain = user.getUsername().split("@");
        String idFromMail = idDomain[0];
        String domain = idDomain[1];

        if (!idFromMail.equalsIgnoreCase(user.getId()))
            throw new UserIdAndMailConflictException(user.getId(), idFromMail);

        switch (user.getId().toLowerCase().toCharArray()[0]){
            case 's':
                if (!domain.equalsIgnoreCase("studenti.polito.it"))
                    throw new InvalidStudentDomainException(domain);
                user.setRoles(Arrays.asList(Roles.ROLE_STUDENT.toString()));
                break;
            case 'd':
                if (!domain.equalsIgnoreCase("polito.it"))
                    throw new InvalidProfessorDomainException(domain);
                user.setRoles(Arrays.asList(Roles.ROLE_PROFESSOR.toString()));
                break;
            default:
                throw new InvalidUserIdException(user.getId());
        }

        if (userRepository.findByUsernameIgnoreCase(user.getUsername()).isPresent())
            throw new UserAlreadyExistException();

        User u = mapper.map(user, User.class);
        u.setPassword(encoder.encode(u.getPassword()));
        userRepository.save(u);

        return user;
    }
}
