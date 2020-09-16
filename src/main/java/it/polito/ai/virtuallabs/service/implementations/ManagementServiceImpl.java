package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.security.dtos.UserDTO;
import it.polito.ai.virtuallabs.security.entities.Roles;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.security.repositories.UserRepository;
import it.polito.ai.virtuallabs.service.ManagementService;
import it.polito.ai.virtuallabs.service.NotificationService;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class ManagementServiceImpl implements ManagementService {

    @Autowired
    private UserRepository userRepository;

    private User getUser(String username){
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User %s not found", username))
        );
    }

    @Override
    public void enableUser(String username) {
        User user = getUser(username);
        user.setEnabled(true);
    }
}
