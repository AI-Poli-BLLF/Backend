package it.polito.ai.virtuallabs.security.controllers;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.repositories.ProfessorRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.security.entities.Roles;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UserInfoController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/API/me")
    public ResponseEntity currentUser(@AuthenticationPrincipal UserDetails userDetails){
        User principal = (User)userDetails;
        Map<Object, Object> model = new HashMap<>();
        if(principal.getRoles().contains(Roles.ROLE_STUDENT.toString())){
            StudentDTO student = teamService.getStudent(principal.getId()).orElse(null);
            if(student != null){
                model.put("name", student.getName());
                model.put("firstName", student.getFirstName());
                model.put("id", student.getId());
                model.put("email", student.getEmail());
                model.put("courses", studentRepository.getCourseNames(principal.getId()));
            }
        }else if(principal.getRoles().contains(Roles.ROLE_PROFESSOR.toString())){
            ProfessorDTO professor = teamService.getProfessor(principal.getId()).orElse(null);
            if(professor != null){
                model.put("name", professor.getName());
                model.put("firstName", professor.getFirstName());
                model.put("id", professor.getId());
                model.put("email", professor.getEmail());
                model.put("courses", professorRepository.getCourseNames(principal.getId()));
            }
        }
        model.put("username", userDetails.getUsername());
        model.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );
        return ResponseEntity.ok(model);
    }
}
