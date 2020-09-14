package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.controllers.utility.ModelHelper;
import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.entities.Draft;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.service.ImageUploadService;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.exceptions.DraftNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.ProfessorNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.images.ImageServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/professors")
public class ProfessorController {
    @Autowired
    private TeamService teamService;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    AssignmentService assignmentService;

    @GetMapping({"/", ""})
    private List<ProfessorDTO> getAllProfessors() {
        return teamService.getProfessors().stream()
                .map(ModelHelper::enrich)
                .collect(Collectors.toList());
    }

    @GetMapping("/{professorId}/courses")
    private List<CourseDTO> getCourses(@PathVariable String professorId){
        try {
            return teamService.getAllCoursesByProfessor(professorId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Professor not found: %s", professorId));
        }
    }

    @GetMapping("/{professorId}")
    private ProfessorDTO getOne(@PathVariable String professorId){
        try{
            return ModelHelper.enrich(teamService.getProfessor(professorId).get());
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Professor not found: %s", professorId));
        }
    }

    @PostMapping("/{professorId}/uploadPhoto")
    private Map<String, String> uploadPhoto(@PathVariable String professorId, @RequestParam("image") MultipartFile image) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("imageRef", imageUploadService.store(image, professorId));
            return map;
        } catch (ImageServiceException | TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping(value = "/{professorId}/photo",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private @ResponseBody byte[] getStudentPhoto(@PathVariable String professorId){
        try {

            return imageUploadService.getImage(professorId);
        } catch (ImageServiceException | TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    //tested
    @PostMapping(value = "/{professorId}/{courseId}/createAssignment")
    @ResponseStatus(value = HttpStatus.CREATED)
    private AssignmentDTO createAssignment(@PathVariable String professorId, @PathVariable String courseId, @RequestBody @Valid AssignmentDTO assignmentDTO){
        if(assignmentService.addAssignment(assignmentDTO, courseId)) {
            return assignmentDTO;
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Assignment already exist: %s", assignmentDTO.getId()));
    }
    //tested
    @PostMapping(value = "/{professorId}/{assignmentId}/uploadAssignmentPhoto")
    private Map<String, String> uploadAssignmentPhoto(@PathVariable String professorId, @PathVariable String assignmentId, @RequestParam("image") MultipartFile image){
        try{
            Map<String, String> map = new HashMap<>();
            map.put("imageRef", imageUploadService.storeAssignmentImage(image, professorId, assignmentId));
            return map;
        }catch (ImageServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
    //tested
    @GetMapping(value = "/{assignmentId}/getProfessor")
    private ProfessorDTO getProfessor(@PathVariable String assignmentId){
        try{
            return assignmentService.getAssignmentProfessor(assignmentId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Assignment not found: %s", assignmentId));
        }
    }

    @PostMapping(value = "/{assignmentId}/{draftId}/review")
    private DraftDTO reviewDraft(@PathVariable String assignmentId, @PathVariable String draftId, int grade){
        try{
            DraftDTO draftDTO = assignmentService.getDraft(draftId);
            if(draftDTO.getState().matches("SUBMITTED")){
                assignmentService.getDraft(draftId).setGrade(grade);
                assignmentService.setDraftStatus(draftId, Draft.State.REVIEWED);
                return draftDTO;
            }
            else return null;
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Draft not found %s", draftId));
        }
    }

    @PostMapping(value = "/{assignmentId}/{draftId}/lock")
    private boolean setLock(@PathVariable String assignmentId, @PathVariable String draftId){
        try{
            assignmentService.getDraft(draftId).setLock(true);
            return true;
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Draft not found %s", draftId));
        }
    }
    @PostMapping(value = "/{assignmentId}/{draftId}/unlock")
    private boolean setUnLock(@PathVariable String assignmentId, @PathVariable String draftId){
        try{
            assignmentService.getDraft(draftId).setLock(false);
            return true;
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Draft not found %s", draftId));
        }
    }

    @GetMapping("/{professorId}/{courseId}/assignments")
    private List<AssignmentDTO> getAssignments(@PathVariable String professorId, @PathVariable String courseId){
        try{
            return assignmentService.getAssignmentPerProfessorPerCourse(professorId, courseId);
        } catch (ProfessorNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("getAssignmentPerProfessor failed"));
        }
    }
}
