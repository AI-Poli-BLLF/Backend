package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/professors")
public class ProfessorController {
    @Autowired
    private TeamService teamService;

    @GetMapping({"/", ""})
    private List<ProfessorDTO> getAllProfessors(){
        return teamService.getProfessors().stream()
                .map(ModelHelper::enrich)
                .collect(Collectors.toList());
    }

    @GetMapping("/{professorId}")
    private ProfessorDTO getOne(@PathVariable String professorId){
        try{
            return ModelHelper.enrich(teamService.getProfessor(professorId).get());
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Professor not found: %s", professorId));
        }
    }
}
