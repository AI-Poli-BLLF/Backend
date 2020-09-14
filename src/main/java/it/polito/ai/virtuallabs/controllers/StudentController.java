package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.controllers.utility.ModelHelper;
import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.DraftDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.TeamDTO;
import it.polito.ai.virtuallabs.entities.Draft;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.ImageUploadService;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.images.ImageServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/students")
public class StudentController {
    @Autowired
    private TeamService teamService;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private AssignmentService assignmentService;

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

    @GetMapping("/{studentId}/teams/{courseName}")
    private List<TeamDTO> getTeamsForStudentForCourse(@PathVariable String studentId, @PathVariable String courseName){
        try {
            List<TeamDTO> teamsForCourse = teamService.getTeamsForCourse(courseName);
            List<TeamDTO> teamsForStudent = teamService.getTeamsForStudent(studentId);
            // return only (inactive) teams of a student referring to the specified course
            teamsForCourse.retainAll(teamsForStudent);
            return teamsForCourse
                    .stream().map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{studentId}/uploadPhoto")
    private Map<String, String> uploadPhoto(@PathVariable String studentId, @RequestParam("image") MultipartFile image){
        // todo: imporre limiti dimensione file
        try {
            Map<String, String> map = new HashMap<>();
            map.put("imageRef", imageUploadService.store(image, studentId));
            return map;
        }catch (ImageServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping(value = "/{studentId}/photo",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private @ResponseBody byte[] getStudentPhoto(@PathVariable String studentId){
        try {

            return imageUploadService.getImage(studentId);
        }catch (ImageServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    //tested
    @GetMapping(value = "/{studentId}/{assignmentId}/{draftId}")
    DraftDTO readAssignment(@PathVariable String studentId, @PathVariable String assignmentId, @PathVariable String draftId){
        try{
            assignmentService.setDraftStatus(draftId, Draft.State.READ);
            return assignmentService.getDraft(draftId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Draft not found for student: %s", studentId));
        }
    }

//    @GetMapping(getAssignmentImage) da fare!

    //tested
    @PostMapping(value = "/{studentId}/{assignmentId}/createDraft")
    @ResponseStatus(value = HttpStatus.CREATED)
    private DraftDTO createDraft(@PathVariable String studentId, @PathVariable String assignmentId, @RequestBody DraftDTO draftDTO){
        if(assignmentService.addDraft(draftDTO, assignmentId, studentId))
//            return ModelHelper.enrich(draftDTO);
            return draftDTO;
        throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Draft already exist: %s", draftDTO.getId()));
    }
    //tested
    @GetMapping(value = "/{studentId}/{draftId}")
    private DraftDTO getDraft(@PathVariable String studentId, @PathVariable String draftId){
        return assignmentService.getDraft(draftId);
    }
    //tested
    @PostMapping(value = "/{studentId}/submitDraft")
    private DraftDTO submitDraft(@PathVariable String studentId, @RequestParam String draftId){
        try{
            assignmentService.setDraftStatus(draftId, Draft.State.SUBMITTED);
            return assignmentService.getDraft(draftId);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Draft not found for student: %s", studentId));
        }
    }
    //tested
    @PostMapping(value = "/{studentId}/{draftId}/uploadDraftPhoto")
    private Map<String, String> uploadDraftPhoto(@PathVariable String studentId, @PathVariable String draftId, @RequestParam("image") MultipartFile image){
        try {
            Map<String, String> map = new HashMap<>();
            map.put("imageRef", imageUploadService.storeDraftImage(image, draftId));
            return map;
        } catch (ImageServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

}
