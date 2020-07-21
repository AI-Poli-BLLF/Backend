package it.ai.polito.lab2.security.controllers;

import it.ai.polito.lab2.dtos.ProfessorDTO;
import it.ai.polito.lab2.dtos.StudentDTO;
import it.ai.polito.lab2.repositories.ProfessorRepository;
import it.ai.polito.lab2.repositories.StudentRepository;
import it.ai.polito.lab2.security.entities.Roles;
import it.ai.polito.lab2.security.entities.User;
import it.ai.polito.lab2.service.TeamService;
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

    @GetMapping("/me")
    public ResponseEntity currentUser(@AuthenticationPrincipal UserDetails userDetails){
        User principal = (User)userDetails;
        Map<Object, Object> model = new HashMap<>();
        if(principal.getRoles().contains(Roles.ROLE_STUDENT.toString())){
            StudentDTO student = teamService.getStudent(principal.getId()).orElse(null);
            if(student != null){
                model.put("name", student.getName());
                model.put("firstName", student.getFirstName());
                model.put("courses", studentRepository.getCourseNames(principal.getId()));
            }
        }else if(principal.getRoles().contains(Roles.ROLE_PROFESSOR.toString())){
            ProfessorDTO professor = teamService.getProfessor(principal.getId()).orElse(null);
            if(professor != null){
                model.put("name", professor.getName());
                model.put("firstName", professor.getFirstName());
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
