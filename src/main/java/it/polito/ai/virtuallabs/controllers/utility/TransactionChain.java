package it.polito.ai.virtuallabs.controllers.utility;

import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.security.dtos.UserDTO;
import it.polito.ai.virtuallabs.security.dtos.UserRegistration;
import it.polito.ai.virtuallabs.security.service.UserManagementService;
import it.polito.ai.virtuallabs.security.service.exceptions.UserAlreadyExistException;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.VMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

import static it.polito.ai.virtuallabs.controllers.utility.ModelHelper.AddCourseRequest;

@Transactional
@Component
public class TransactionChain {

    @Autowired
    private TeamService teamService;
    @Autowired
    private VMService vmService;

    public CourseDTO createCourseWithModel(AddCourseRequest addCourseRequest){
        CourseDTO courseDTO = teamService.addCourse(addCourseRequest.getCourse(), addCourseRequest.getProfessorId());
        vmService.createVMModel(addCourseRequest.getVmModel(), addCourseRequest.getCourse().getName());
        return courseDTO;
    }

    public void registerUser(UserManagementService userManagementService, UserRegistration user){
        UserDTO userDTO = new UserDTO(user.getUserId(), user.getEmail(), user.getPassword(), new ArrayList<>());
        userManagementService.createUser(userDTO);

        switch (user.getUserId().toLowerCase().toCharArray()[0]){
            case 's':
                StudentDTO s = new StudentDTO(user.getUserId(), user.getName(), user.getFirstName());
                if (!teamService.addStudent(s))
                    throw new UserAlreadyExistException();
                break;
            case 'd':
                ProfessorDTO p = new ProfessorDTO(user.getUserId(), user.getName(), user.getFirstName(), new ArrayList<>());
                if (!teamService.addProfessor(p))
                    throw new UserAlreadyExistException();
                break;
            default:
                //non dovrebbe mai arrivarci perchè c'è il controllo del pattern su userId
                throw new UserAlreadyExistException();
        }
    }
}
