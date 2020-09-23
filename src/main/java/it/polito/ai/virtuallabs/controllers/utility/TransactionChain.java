package it.polito.ai.virtuallabs.controllers.utility;

import it.polito.ai.virtuallabs.dtos.AssignmentDTO;
import it.polito.ai.virtuallabs.dtos.CorrectionDTO;
import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.DraftDTO;
import it.polito.ai.virtuallabs.security.dtos.UserDTO;
import it.polito.ai.virtuallabs.security.dtos.UserRegistration;
import it.polito.ai.virtuallabs.security.service.UserManagementService;
import it.polito.ai.virtuallabs.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

import static it.polito.ai.virtuallabs.controllers.utility.ModelHelper.AddCourseRequest;

@Transactional
@Component
public class TransactionChain {

    @Autowired
    private TeamService teamService;
    @Autowired
    private VMService vmService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private ImageUploadService uploadService;

    public CourseDTO updateCourseWithModel(AddCourseRequest addCourseRequest){
        CourseDTO courseDTO = teamService.addCourse(addCourseRequest.getCourse(), addCourseRequest.getProfessorId());
        vmService.createVMModel(addCourseRequest.getVmModel(), addCourseRequest.getCourse().getName());
        return courseDTO;
    }

    public CourseDTO updateCourseWithModel(String oldCourseName, AddCourseRequest editCourseRequest){
        vmService.updateVMModel(editCourseRequest.getVmModel(), oldCourseName);
        return teamService.updateCourse(oldCourseName, editCourseRequest.getCourse());
    }

    public void registerUser(UserManagementService userManagementService, UserRegistration user){
        UserDTO userDTO = new UserDTO(user.getUserId(), user.getEmail(), user.getPassword(), new ArrayList<>());
        userManagementService.createUser(userDTO);
        notificationService.sendConfirmEmailRegistration(user.getEmail(), user.getFirstName(), user.getName());
    }

    public AssignmentDTO addAssignmentAndUploadImage(AssignmentDTO assignmentDTO, String courseName, MultipartFile image){
        AssignmentDTO a = assignmentService.addAssignment(assignmentDTO, courseName);
        uploadService.storeAssignmentImage(image, courseName, a.getId());
        return a;
    }

    public byte[] getAssignmentImageAndReadAssignment(Long assignmentId, String studentId, String courseName){
        assignmentService.readAssigment(assignmentId, studentId, courseName);
        return uploadService.getAssignmentImage(courseName, assignmentId);
    }

    public CorrectionDTO correctDraftAndUploadImage(String courseName, Long assignmentId, Long draftId,
                                                    int grade, MultipartFile image) {
        CorrectionDTO corr =  assignmentService.correctDraft(courseName, assignmentId, draftId, grade);
        uploadService.storeCorrectionImage(courseName, assignmentId, draftId, corr.getId(), image);
        return corr;
    }

    public DraftDTO submitDraftAndUploadImage(String studentId, String courseName, Long assignmentId, MultipartFile image) {
        DraftDTO draft = assignmentService.submitDraft(studentId, courseName, assignmentId);
        uploadService.storeDraftImage(studentId, courseName, assignmentId, draft.getId(), image);
        return draft;
    }
}
