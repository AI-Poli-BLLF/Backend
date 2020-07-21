package it.ai.polito.lab2.security.controllers;

import it.ai.polito.lab2.security.JwtTokenProvider;
import it.ai.polito.lab2.security.dtos.JwtRequest;
import it.ai.polito.lab2.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/authenticate")
    private ResponseEntity provideToken(@RequestBody JwtRequest request){
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
}
