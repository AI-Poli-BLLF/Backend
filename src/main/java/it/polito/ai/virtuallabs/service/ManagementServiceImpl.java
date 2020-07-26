package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.security.dtos.UserDTO;
import it.polito.ai.virtuallabs.security.entities.Roles;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.security.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class ManagementServiceImpl implements ManagementService{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ModelMapper mapper;

    @PreAuthorize("hasAnyRole('ROLE_PROFESSOR', 'ROLE_ADMIN')")
    @Override
    public boolean createStudentUser(StudentDTO studentDTO) {

        String username = studentDTO.getEmail();
        String userId = studentDTO.getId();
        String password = generateCommonLangPassword();

        User user = User.builder().id(userId).username(username).password(encoder.encode(password))
                .roles(Arrays.asList(Roles.ROLE_STUDENT.toString())).build();

        try {
            createUserAndSendMail(user, password, studentDTO.getFirstName());
        }catch (Exception e){
            return false;
        }

        return true;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public boolean createProfessorUser(ProfessorDTO professorDTO) {

        String username = professorDTO.getEmail();
        String userId = professorDTO.getId();
        String password = generateCommonLangPassword();

        User user = User.builder().id(userId).username(username).password(encoder.encode(password))
                .roles(Arrays.asList(Roles.ROLE_PROFESSOR.toString())).build();

        try {
            createUserAndSendMail(user, password, professorDTO.getFirstName());
        }catch (Exception e){
            return false;
        }

        return true;
    }

    public String generateCommonLangPassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                .concat(specialChar)
                .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        return pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private void createUserAndSendMail(User user, String plainPassword, String firstName){
        if(userRepository.findByUsernameIgnoreCase(user.getUsername()).isPresent())
            throw new IllegalStateException();

        userRepository.save(user);
        UserDTO dto = mapper.map(user, UserDTO.class);
        dto.setPassword(plainPassword);
        sendPassword(dto, firstName);
    }

    private void sendPassword(UserDTO user, String firstName) {
        String subject = "[Applicazioni Internet] Teams";
        String body = String.format("Ciao %s, benvenuto su Teams!\n" +
                "Il tuo account è stato creato con successo!\n" +
                "Per accedere utilizza i dati forniti di seguito: \n" +
                "Username: %s\n" +
                "Passowrd: %s\n", firstName, user.getUsername(),user.getPassword());
        notificationService.sendMessage(user.getUsername(), subject, body);
    }
}
