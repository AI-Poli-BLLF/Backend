package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.controllers.utility.ModelHelper;
import it.polito.ai.virtuallabs.controllers.utility.TransactionChain;
import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.ImageUploadService;
import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.AssignmentServiceException;
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
    @Autowired
    private TransactionChain transactionChain;
    @Autowired
    private NotificationService notificationService;

    @GetMapping({"", "/"})
    private List<StudentDTO> all() {
        return teamService.getAllStudents().stream().map(ModelHelper::enrich).collect(Collectors.toList());
    }

    @GetMapping("/{studentId}")
    private StudentDTO getOne(@PathVariable String studentId) {
        try {
            return ModelHelper.enrich(teamService.getStudent(studentId).get());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Student not found: %s", studentId));
        }
    }
//
//    @PostMapping({"", "/"})
//    @ResponseStatus(value = HttpStatus.CREATED)
//    private StudentDTO addStudent(@RequestBody @Valid StudentDTO student) {
//
//        if (teamService.addStudent(student))
//            return ModelHelper.enrich(student);
//
//        throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Student already exist: %s", student.getId()));
//    }

    @GetMapping("{studentId}/courses")
    private List<CourseDTO> getCourses(@PathVariable String studentId) {
        try {
            return teamService.getCourses(studentId).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        } catch (TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("{studentId}/available-courses")
    private List<CourseDTO> getAvailableCoursesForStudent(@PathVariable String studentId) {
        try {
            return teamService.getAvailableCoursesForStudent(studentId).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        } catch (TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{studentId}/teams")
    private List<TeamDTO> getTeamsForStudent(@PathVariable String studentId) {
        try {
            return teamService.getTeamsForStudent(studentId)
                    .stream().map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        } catch (TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{studentId}/teams/{courseName}")
    private List<TeamDTO> getTeamsForStudentForCourse(@PathVariable String studentId, @PathVariable String courseName) {
        try {
            List<TeamDTO> teamsForCourse = teamService.getTeamsForCourse(courseName);
            List<TeamDTO> teamsForStudent = teamService.getTeamsForStudent(studentId);
            // return only (inactive) teams of a student referring to the specified course
            teamsForCourse.retainAll(teamsForStudent);
            return teamsForCourse
                    .stream().map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        } catch (TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{studentId}/uploadPhoto")
    private Map<String, String> uploadPhoto(@PathVariable String studentId, @RequestParam("image") MultipartFile image) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("imageRef", imageUploadService.store(image, studentId));
            return map;
        } catch (ImageServiceException | TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping(value = "/{studentId}/photo",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private @ResponseBody
    byte[] getStudentPhoto(@PathVariable String studentId) {
        try {

            return imageUploadService.getImage(studentId);
        } catch (ImageServiceException | TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping(value = "/{studentId}/courses/{courseName}/assignments/{assignmentId}/image",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private byte[] readAssignment(@PathVariable String studentId, @PathVariable String courseName, @PathVariable String assignmentId){
        try{
            return transactionChain.getAssignmentImageAndReadAssignment(Long.parseLong(assignmentId), studentId, courseName);
        } catch (ImageServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Assignment not found for student: %s", studentId));
        }
    }

    @GetMapping(value = "/{studentId}/courses/{courseName}/assignments/{assignmentId}/draft/{draftId}/image",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private byte[] getDraftImage(@PathVariable String studentId, @PathVariable String courseName,
                                 @PathVariable Long assignmentId, @PathVariable Long draftId) {
        try {
            return imageUploadService.getDraftImageStudent(studentId, courseName, assignmentId, draftId);
        } catch (ImageServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping(value = "/{studentId}/courses/{courseName}/assignments/{assignmentId}/drafts")
    private List<DraftDTO> getDraftsForStudent(@PathVariable String studentId, @PathVariable String courseName, @PathVariable Long assignmentId) {
        try {
            return assignmentService.getDraftsForStudent(studentId, courseName, assignmentId);
        } catch (AssignmentServiceException | TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("getDraftsForStudent failed"));
        }
    }

    //tested
    @PostMapping("/{studentId}/courses/{courseName}/assignments/{assignmentId}/drafts")
    private DraftDTO submitDraft(@PathVariable String studentId, @PathVariable String courseName, @PathVariable Long assignmentId,
                                 @RequestParam("image") MultipartFile image) {
        try {
            return transactionChain.submitDraftAndUploadImage(studentId, courseName, assignmentId, image);
        } catch (AssignmentServiceException | TeamServiceException | ImageServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
//            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Draft not found for student: %s", studentId));
        }
    }

    @GetMapping(value = "/{studentId}/courses/{courseName}/assignments/{assignmentId}/draft/{draftId}/correction-image",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private byte[] getCorrectionImageForDraft(@PathVariable String studentId, @PathVariable String courseName,
                                 @PathVariable Long assignmentId, @PathVariable Long draftId) {
        try {
            return imageUploadService.getCorrectionImageForDraft(studentId, courseName, assignmentId, draftId);
        } catch (ImageServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
