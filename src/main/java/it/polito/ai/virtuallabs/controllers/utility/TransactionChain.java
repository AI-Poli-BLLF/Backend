package it.polito.ai.virtuallabs.controllers.utility;

import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.security.dtos.UserDTO;
import it.polito.ai.virtuallabs.security.dtos.UserRegistration;
import it.polito.ai.virtuallabs.security.service.UserManagementService;
import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.VMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public CourseDTO createCourseWithModel(AddCourseRequest addCourseRequest){
        CourseDTO courseDTO = teamService.addCourse(addCourseRequest.getCourse(), addCourseRequest.getProfessorId());
        vmService.createVMModel(addCourseRequest.getVmModel(), addCourseRequest.getCourse().getName());
        return courseDTO;
    }

    public void registerUser(UserManagementService userManagementService, UserRegistration user){
        UserDTO userDTO = new UserDTO(user.getUserId(), user.getEmail(), user.getPassword(), new ArrayList<>());
        userManagementService.createUser(userDTO);
        notificationService.sendConfirmEmailRegistration(user.getEmail(), user.getFirstName(), user.getName());
    }
}