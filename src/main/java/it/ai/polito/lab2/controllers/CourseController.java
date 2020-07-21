package it.ai.polito.lab2.controllers;

import it.ai.polito.lab2.dtos.CourseDTO;
import it.ai.polito.lab2.dtos.StudentDTO;
import it.ai.polito.lab2.dtos.TeamDTO;
import it.ai.polito.lab2.service.NotificationService;
import it.ai.polito.lab2.service.TeamService;
import it.ai.polito.lab2.service.exceptions.CourseNotFoundException;
import it.ai.polito.lab2.service.exceptions.TeamNotFoundException;
import it.ai.polito.lab2.service.exceptions.TeamServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/courses")
public class CourseController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping({"", "/"})
    private List<CourseDTO> all(){
        return teamService.getAllCourses().stream().map(ModelHelper::enrich).collect(Collectors.toList());
    }

    @GetMapping("/{courseName}")
    private CourseDTO getOne(@PathVariable String courseName){
        try{
            return ModelHelper.enrich(teamService.getCourse(courseName).get());
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Course not found: %s", courseName));
        }
    }

    @PostMapping({"", "/"})
    @ResponseStatus(value = HttpStatus.CREATED)
    private CourseDTO addCourse(@RequestBody @Valid CourseDTO course){
        if(course.getMin() <= course.getMax() && teamService.addCourse(course)){
            return ModelHelper.enrich(course);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Course already exist: %s", course.getName()));
    }

    @GetMapping("/{courseName}/enrolled")
    private List<StudentDTO> enrolledStudents(@PathVariable String courseName){
        try {
            return teamService.getEnrolledStudents(courseName).stream()
                    .map(ModelHelper::enrich).collect(Collectors.toList());
        }catch (CourseNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{courseName}/enrollOne")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void enrollOneStudent(@PathVariable String courseName, @RequestBody @Valid String studentID){
        try {
            if(!teamService.addStudentToCourse(studentID, courseName))
                throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Student <%s> is already enrolled", studentID));
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }


    @PostMapping("/{courseName}/enrollMany")
    @ResponseStatus(value = HttpStatus.CREATED)
    private List<Boolean> enrollStudents(@PathVariable String courseName, @RequestParam("file") MultipartFile file){

        if(!file.getContentType().equals("text/csv"))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "text/csv file required");

        try(Reader r = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            return teamService.addAndEnroll(r, courseName);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An error occurred while reading the file, please try again later");
        } catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("{courseName}/proposeTeam")
    @ResponseStatus(value = HttpStatus.CREATED)
    private TeamDTO proposeTeam(@PathVariable String courseName, @RequestBody @Valid ModelHelper.TeamProposal proposal){
        try{
            CourseDTO d = new CourseDTO();
            TeamDTO team = teamService.proposeTeam(courseName, proposal.getTeamName(), proposal.getMemberIds());
            notificationService.notifyTeam(team, proposal.getMemberIds());
            return team;
        }catch (TeamServiceException e){
            String message = e.getMessage() == null ? "Error during team proposal" : e.getMessage();
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    @PutMapping("{courseName}/enable")
    private void enableCourse(@PathVariable String courseName){
        try{
            teamService.enableCourse(courseName);
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("{courseName}/disable")
    private void disableCourse(@PathVariable String courseName){
        try{
            teamService.disableCourse(courseName);
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams")
    private List<TeamDTO> getTeamsForCourse(@PathVariable String courseName){
        try {
            return teamService.getTeamForCourse(courseName)
                    .stream().map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/teams/{teamId}")
    private TeamDTO getTeamById(@PathVariable String teamId){
        try{
            Long id = Long.valueOf(teamId);
            return ModelHelper.enrich(teamService.getTeam(id).get());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, new TeamNotFoundException(Long.valueOf(teamId)).getMessage());
        }
    }

    @GetMapping("/teams/{teamId}/members")
    private List<StudentDTO> getMembers(@PathVariable String teamId){
        try{
            Long id = Long.valueOf(teamId);
            return teamService.getMembers(id).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/studentsInTeam")
    private List<StudentDTO> getStudentsInTeams(@PathVariable String courseName){
        try{
            return teamService.getStudentsInTeams(courseName).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/availableStudents")
    private List<StudentDTO> getAvailableStudents(@PathVariable String courseName){
        try{
            return teamService.getAvailableStudents(courseName).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }



    /*
    --------------------------- EXCEPTION HANDLERS ---------------------------------------
     */

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "BAD_REQUEST");
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MultipartException.class)
    public Map<String, String> handleValidationExceptions(MultipartException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "BAD_REQUEST");
        errors.put("message", ex.getMessage());
        return errors;
    }
}
