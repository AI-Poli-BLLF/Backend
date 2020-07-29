package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;
import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.repositories.CourseRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.repositories.TeamRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMConfigRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMInstanceRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMModelRepository;
import it.polito.ai.virtuallabs.service.exceptions.CourseNotEnabledException;
import it.polito.ai.virtuallabs.service.exceptions.CourseNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.TeamNotBelongToCourseException;
import it.polito.ai.virtuallabs.service.exceptions.TeamNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMModelAlreadyExistException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMModelNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VMServiceImpl implements VMService{

    @Autowired
    private VMModelRepository vmModelRepository;
    @Autowired
    private VMConfigRepository vmConfigRepository;
    @Autowired
    private VMInstanceRepository vmInstanceRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private ManagementServiceImpl managementService;

    //@PreAuthorize("hasRole('PROFESSOR') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public void createVMModel(VMModelDTO vmModelDTO, String courseName) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se è già stata creata un VMModel associato al corso lancia una eccezione
        if (vmModelRepository.findByIdIgnoreCase(courseName).isPresent() || c.getVmModel() != null)
            throw new VMModelAlreadyExistException(courseName);

        VMModel vmModel = mapper.map(vmModelDTO, VMModel.class);
        vmModel.setCourse(c);
        vmModelRepository.save(vmModel);
    }

    //@PreAuthorize("hasRole('PROFESSOR') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public void setVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        if (c.getVmModel() == null)
            throw new VMModelNotFoundException(courseName);

        Team t = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );

        if (!t.getCourse().getName().equals(courseName))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        VMConfig vmConfig = mapper.map(vmConfigDTO, VMConfig.class);
        vmConfig.setTeam(t);
        vmConfigRepository.save(vmConfig);
    }
}
