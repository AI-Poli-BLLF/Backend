package it.polito.ai.virtuallabs.security.controllers;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.security.JwtTokenProvider;
import it.polito.ai.virtuallabs.security.dtos.JwtRequest;
import it.polito.ai.virtuallabs.security.dtos.UserDTO;
import it.polito.ai.virtuallabs.security.dtos.UserRegistration;
import it.polito.ai.virtuallabs.security.repositories.UserRepository;
import it.polito.ai.virtuallabs.security.service.UserManagementService;
import it.polito.ai.virtuallabs.security.service.exceptions.UserServiceException;
import it.polito.ai.virtuallabs.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class JwtController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepository repository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private UserManagementService userManagementService;

    @PostMapping("/authenticate")
    private ResponseEntity provideToken(@RequestBody @Valid JwtRequest request){
        try {
            String username = request.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));

            String token = jwtTokenProvider.createToken(username, repository.findByUsernameIgnoreCase(username)
            .orElseThrow(()-> new UsernameNotFoundException("Username " + username + "not found"))
            .getRoles());

            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            return ResponseEntity.ok(model);
        }catch (AuthenticationException e){
            throw new BadCredentialsException("Invalid username/password");
        }
    }

    @PostMapping("/register")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void registerUser(@RequestBody @Valid UserRegistration user){
        try{
            UserDTO userDTO = new UserDTO(user.getUserId(), user.getEmail(), user.getPassword(), new ArrayList<>());
            userManagementService.createUser(userDTO);
        }catch (UserServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        /*
        TODO: gestire il chaining di transazioni
         */
        switch (user.getUserId().toLowerCase().toCharArray()[0]){
            case 's':
                StudentDTO s = new StudentDTO(user.getUserId(), user.getName(), user.getFirstName());
                if (!teamService.addStudent(s))
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Student already exist");
                break;
            case 'd':
                ProfessorDTO p = new ProfessorDTO(user.getUserId(), user.getName(), user.getFirstName(), new ArrayList<>());
                if (!teamService.addProfessor(p))
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Student already exist");
                break;
            default:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid User id");
        }
    }
}
