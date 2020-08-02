package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;
import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.repositories.CourseRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.repositories.TeamRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMConfigRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMInstanceRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMModelRepository;
import it.polito.ai.virtuallabs.service.exceptions.*;
import it.polito.ai.virtuallabs.service.exceptions.vms.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        // Se il team non appartiene a quel corso lancia una eccezione
        if (!t.getCourse().getName().equals(courseName))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        VMConfig vmConfig = mapper.map(vmConfigDTO, VMConfig.class);
        vmConfig.setTeam(t);
        vmConfigRepository.save(vmConfig);
    }

    //@PreAuthorize("@securityApiAuth.isMe(#studentId)")
    @Override
    public void createVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, String studentId) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        Student s = studentRepository.findByIdIgnoreCase(studentId).orElseThrow(
                () -> new StudentNotFoundException(studentId)
        );

        //Controllo se lo studente è iscritto al corso
        Set<String> enrolledCourses = studentRepository.getCourseNames(studentId)
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
        if (!enrolledCourses.contains(courseName.toLowerCase()))
            throw new StudentNotEnrolledException(studentId, courseName);


        //Controllo se appartiene a un team per il corso
        Team courseTeam = s.getTeams().stream()
                .filter(t-> t.getCourse().getName().toLowerCase().equals(courseName.toLowerCase()))
                .findFirst().orElseThrow(
                        () -> new NoTeamException(studentId, courseName)
                );

        //Controllo se è stato settato un modello di VM per il corso
        VMModel vmModel = vmModelRepository.findByIdIgnoreCase(courseName).orElseThrow(
                () -> new NoVMModelException(courseName)
        );

        //Controllo se è stata settata una configurazione per il team
        VMConfig config = courseTeam.getVmConfig();
        if (config == null)
            throw new NoVMConfigException(courseTeam.getName(), courseName);

        //Controllo delle specifiche settate
        Map<String, Integer> limits = config.getConfig();
        Map<String, Integer> vmConfig = vmInstanceDTO.getConfig();

        List<VMInstance> instances = courseTeam.getVms();
        int totalVms = instances.size();

        if (totalVms >= config.getMaxVm())
            throw new VMNumberExceedException(courseTeam.getName(), courseName, config.getMaxVm());

        vmConfig.forEach((key, value) -> {
            int limit = limits.get(key);
            if (value > limit)
                throw new VMLimitExceedException(courseTeam.getName(), courseName, key, limit, value);
        });

        //Create VMInstance
        VMInstance vmInstance = mapper.map(vmInstanceDTO, VMInstance.class);
        vmInstance.setVmModel(vmModel);
        vmInstance.setTeam(courseTeam);
        vmInstance.addOwner(s);

        vmInstanceRepository.save(vmInstance);
    }

    //@PreAuthorize("@securityApiAuth.isMe(#ownerId)")
    @Override
    public void shareVMOwnership(Long vmInstanceId, String ownerId, String teammateId) {
        VMInstance vmInstance = vmInstanceRepository.findById(vmInstanceId).orElseThrow(
                () -> new VMInstanceNotFoundException(vmInstanceId)
        );

        Student owner = studentRepository.findByIdIgnoreCase(ownerId).orElseThrow(
                () -> new StudentNotFoundException(ownerId)
        );

        Student teammate = studentRepository.findByIdIgnoreCase(teammateId).orElseThrow(
                () -> new StudentNotFoundException(ownerId)
        );

        //Se i due studenti non appartengono allo stesso team, lancia una eccezione
        if (!vmInstance.getTeam().getMembers().containsAll(Arrays.asList(owner, teammate)))
            throw new StudentsNotInSameTeamException(ownerId, teammateId, vmInstance.getTeam().getName());

        //Verifico che sia effettivamente l'owner della VM
        if (!vmInstance.getOwners().contains(owner))
            throw new StudentNotOwnerException(ownerId, vmInstanceId);

        //Verifico che il corso sia ancora attivo
        Course c = vmInstance.getTeam().getCourse();
        if (!c.isEnabled())
            throw new CourseNotEnabledException(c.getName());

        vmInstance.addOwner(teammate);
    }

    //@PreAuthorize("@securityApiAuth.isMe(#ownerId)")
    @Override
    public void bootVMInstance(Long vmInstanceId, String ownerId) {
        VMInstance vmInstance = vmInstanceRepository.findById(vmInstanceId).orElseThrow(
                () -> new VMInstanceNotFoundException(vmInstanceId)
        );

        Student owner = studentRepository.findByIdIgnoreCase(ownerId).orElseThrow(
                () -> new StudentNotFoundException(ownerId)
        );

        if (!vmInstance.getOwners().contains(owner))
            throw new StudentNotOwnerException(ownerId, vmInstanceId);

        vmInstance.setActive(true);
    }

    //@PreAuthorize("@securityApiAuth.isMe(#ownerId)")
    @Override
    public void shutdownVMInstance(Long vmInstanceId, String ownerId) {
        VMInstance vmInstance = vmInstanceRepository.findById(vmInstanceId).orElseThrow(
                () -> new VMInstanceNotFoundException(vmInstanceId)
        );

        Student owner = studentRepository.findByIdIgnoreCase(ownerId).orElseThrow(
                () -> new StudentNotFoundException(ownerId)
        );

        if (!vmInstance.getOwners().contains(owner))
            throw new StudentNotOwnerException(ownerId, vmInstanceId);

        vmInstance.setActive(false);
    }

    //@PreAuthorize("@securityApiAuth.isMe(#ownerId)")
    @Override
    public void deleteVMInstance(Long vmInstanceId, String ownerId) {
        VMInstance vmInstance = vmInstanceRepository.findById(vmInstanceId).orElseThrow(
                () -> new VMInstanceNotFoundException(vmInstanceId)
        );

        Student owner = studentRepository.findByIdIgnoreCase(ownerId).orElseThrow(
                () -> new StudentNotFoundException(ownerId)
        );

        if (!vmInstance.getOwners().contains(owner))
            throw new StudentNotOwnerException(ownerId, vmInstanceId);

        List<Student> owners = vmInstance.getOwners();

        owners.forEach(vmInstance::removeOwner);
        vmInstance.getTeam().getVms().remove(vmInstance);
        vmInstance.getVmModel().getVmInstances().remove(vmInstance);

        vmInstanceRepository.delete(vmInstance);
    }
}
