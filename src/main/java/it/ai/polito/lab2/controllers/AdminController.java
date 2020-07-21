package it.ai.polito.lab2.controllers;

import it.ai.polito.lab2.dtos.ProfessorDTO;
import it.ai.polito.lab2.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/API/admin")
public class AdminController {

    @Autowired
    private TeamService teamService;

    @PostMapping("/addProfessor")
    @ResponseStatus(value = HttpStatus.CREATED)
    private ProfessorDTO addProfessor(@RequestBody @Valid ProfessorDTO professor){
        if(teamService.addProfessor(professor))
            return professor;

        throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Professor already exist: %s", professor.getId()));
    }
}
