package it.polito.ai.virtuallabs.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.virtuallabs.controllers.utility.ModelHelper;
import it.polito.ai.virtuallabs.controllers.utility.TransactionChain;
import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.dtos.tokens.BasicToken;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.service.ImageUploadService;
import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.exceptions.assignments.AssignmentServiceException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.DraftNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.NotificationException;
import it.polito.ai.virtuallabs.service.exceptions.ProfessorNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.images.ImageServiceException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.ws.rs.Path;
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
    private AssignmentService assignmentService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionChain transactionChain;

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

    @PostMapping(value = "/{professorId}/courses/{courseId}/assignments")
    @ResponseStatus(value = HttpStatus.CREATED)
    private AssignmentDTO createAssignment(@PathVariable String professorId, @PathVariable String courseId,
                                           @RequestParam("json") String assignmentString, @RequestParam("image") MultipartFile image,
                                           @Autowired ObjectMapper mapper){
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            AssignmentDTO assignmentDTO = mapper.readValue(assignmentString, AssignmentDTO.class);
            System.out.println(assignmentDTO);
            return ModelHelper.enrich(transactionChain.addAssignmentAndUploadImage(professorId, assignmentDTO, courseId, image), courseId);
        }catch (TeamServiceException | AssignmentServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    //tested
//    @PostMapping(value = "/{professorId}/courses/{courseId}/assignments/{assignmentId}/uploadAssignmentPhoto")
//    @ResponseStatus(value = HttpStatus.CREATED)
//    private Map<String, String> uploadAssignmentPhoto(@PathVariable String professorId, @PathVariable String courseId,
//                                                      @PathVariable Long assignmentId, @RequestParam("image") MultipartFile image){
//        try{
//            Map<String, String> map = new HashMap<>();
//            map.put("imageRef", imageUploadService.storeAssignmentImage(image, professorId, assignmentId));
//            return map;
//        }catch (ImageServiceException | TeamServiceException e){
//            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
//        }
//    }
    //tested
    @GetMapping(value = "/assignments/{assignmentId}/getProfessor")
    private ProfessorDTO getProfessor(@PathVariable Long assignmentId){
        try{
            return assignmentService.getAssignmentProfessor(assignmentId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Assignment not found: %s", assignmentId));
        }
    }

    @PostMapping(value = "{professorId}/courses/{courseName}/assignments/{assignmentId}/drafts/{draftId}/correction")
    @ResponseStatus(value = HttpStatus.CREATED)
    private CorrectionDTO correctDraft(@PathVariable String professorId, @PathVariable String courseName,
                                  @PathVariable Long assignmentId, @PathVariable Long draftId, @RequestParam("image") MultipartFile image){
        try{
            return transactionChain.correctDraftAndUploadImage(professorId, courseName, assignmentId, draftId, false, image);
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Draft not found %s", draftId));
        }
    }

    @PutMapping(value = "/assignments/{assignmentId}/drafts/{draftId}/lock")
    private boolean setLock(@PathVariable Long assignmentId, @PathVariable Long draftId){
        try{
            assignmentService.setDraftLock(draftId);
            return true;
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Draft not found %s", draftId));
        }
    }
    @PutMapping(value = "/assignments/{assignmentId}/drafts/{draftId}/unlock")
    private boolean setUnLock(@PathVariable Long assignmentId, @PathVariable Long draftId){
        try{
//            assignmentService.getDraft(draftId).setLock(false);
            assignmentService.setDraftUnlock(draftId);
            return true;
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Draft not found %s", draftId));
        }
    }

    @GetMapping("/{professorId}/courses/{courseId}/assignments")
    private List<AssignmentDTO> getAssignments(@PathVariable String professorId, @PathVariable String courseId){
        try{
            return assignmentService.getAssignmentPerProfessorPerCourse(professorId, courseId);
        } catch (ProfessorNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("getAssignmentPerProfessor failed"));
        }
    }

    @GetMapping("/{professorId}/courses/{courseId}/assignments/{assignmentId}/drafts")
    private List<DraftDTO> getDrafts(@PathVariable String professorId, @PathVariable String courseId, @PathVariable Long assignmentId){
        try{
            return assignmentService.getDrafts(professorId, courseId, assignmentId);
        } catch (AssignmentServiceException | TeamServiceException  e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("getDrafts failed"));
        }
    }

    @GetMapping("/{professorId}/courses/{courseName}/assignments/{assignmentId}/drafts/{draftId}/student")
    private StudentDTO getStudentForDraft(@PathVariable String professorId, @PathVariable String courseName, @PathVariable Long assignmentId, @PathVariable Long draftId){
        try {
            return this.assignmentService.getStudentForDraft(professorId, courseName, assignmentId, draftId);
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "getStudentForDraft failed");
        }
    }


    @PostMapping("/{senderProfessorId}/courses/{courseName}/cooperate")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void cooperateWith(@PathVariable String senderProfessorId, @PathVariable String courseName, @RequestBody List<String> receiverProfessorIds){
        try {
            notificationService.cooperateWithProfessor(senderProfessorId, receiverProfessorIds, courseName);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping(value = "/{professorId}/courses/{courseName}/assignments/{assignmentId}/image",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private byte[] getDraftImage(@PathVariable String professorId, @PathVariable String courseName,
                                 @PathVariable Long assignmentId){
        try{
            // todo: secondo me mancano controlli su getAssignmentImage che può essere chiamata da stud o da prof
            return imageUploadService.getAssignmentImage(assignmentId);
        }catch (ImageServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/{professorId}/courses/{courseName}/assignments/{assignmentId}/drafts/{draftId}/evaluate")
    @ResponseStatus(value = HttpStatus.CREATED)
    private int evaluateDraft(@PathVariable String professorId, @PathVariable String courseName, @PathVariable Long assignmentId, @PathVariable Long draftId, int grade){
        try{
            return this.assignmentService.evaluateDraft(professorId, courseName, assignmentId, draftId, grade);
        } catch (AssignmentServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

//    addSolution


    @GetMapping("/{professorId}/notifications")
    private List<BasicToken> getNotifications(@PathVariable String professorId){
        try {
            return notificationService.getProfessorNotification(professorId);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
