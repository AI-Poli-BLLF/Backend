package it.polito.ai.virtuallabs.controllers.utility;

import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.VMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
}
