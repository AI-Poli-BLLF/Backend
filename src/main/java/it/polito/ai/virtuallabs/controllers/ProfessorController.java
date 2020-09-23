package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.controllers.utility.ModelHelper;
import it.polito.ai.virtuallabs.controllers.utility.TransactionChain;
import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.dtos.tokens.BasicToken;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.ImageUploadService;
import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.exceptions.NotificationException;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.AssignmentServiceException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.DraftNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.images.ImageServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
        try{
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

    @PostMapping("/{senderProfessorId}/courses/{courseName}/cooperate")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void cooperateWith(@PathVariable String senderProfessorId, @PathVariable String courseName, @RequestBody List<String> receiverProfessorIds){
        try {
            notificationService.cooperateWithProfessor(senderProfessorId, receiverProfessorIds, courseName);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{professorId}/notifications")
    private List<BasicToken> getNotifications(@PathVariable String professorId){
        try {
            return notificationService.getProfessorNotification(professorId);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
