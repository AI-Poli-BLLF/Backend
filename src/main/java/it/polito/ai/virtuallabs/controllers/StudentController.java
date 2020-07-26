package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.TeamDTO;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/students")
public class StudentController {
    @Autowired
    private TeamService teamService;

    @GetMapping({"", "/"})
    private List<StudentDTO> all(){
        return teamService.getAllStudents().stream().map(ModelHelper::enrich).collect(Collectors.toList());
    }

    @GetMapping("/{studentId}")
    private StudentDTO getOne(@PathVariable String studentId){
        try{
            return ModelHelper.enrich(teamService.getStudent(studentId).get());
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Student not found: %s", studentId));
        }
    }

    @PostMapping({"","/"})
    @ResponseStatus(value = HttpStatus.CREATED)
    private StudentDTO addStudent(@RequestBody @Valid StudentDTO student){

        if(teamService.addStudent(student))
            return ModelHelper.enrich(student);

        throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Student already exist: %s", student.getId()));
    }

    @GetMapping("{studentId}/courses")
    private List<CourseDTO> getCourses(@PathVariable String studentId){
        try {
            return teamService.getCourses(studentId).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{studentId}/teams")
    private List<TeamDTO> getTeamsForStudent(@PathVariable String studentId){
        try {
            return teamService.getTeamsForStudent(studentId)
                    .stream().map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
