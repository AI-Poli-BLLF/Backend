package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.StudentDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    public VMModelDTO createVMModel(VMModelDTO vmModelDTO, String courseName) {
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
        return mapper.map(vmModelRepository.save(vmModel), VMModelDTO.class);
    }

    @Override
    public VMModelDTO updateVMModel(VMModelDTO vmModelDTO, String courseName) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se è già stata creata un VMModel associato al corso lancia una eccezione
        VMModel vmModel = vmModelRepository.findByIdIgnoreCase(courseName).orElseThrow(
                () -> new VMModelNotFoundException(courseName)
        );

        vmModel.setOs(VMModel.OS.valueOf(vmModelDTO.getOs()));
        vmModel.setVersion(vmModelDTO.getVersion());
        return mapper.map(vmModelRepository.save(vmModel), VMModelDTO.class);
    }

    //@PreAuthorize("hasRole('PROFESSOR') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public VMConfigDTO createVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se nessun modello è ancora stato settato lancia una eccezione
        if (c.getVmModel() == null)
            throw new VMModelNotFoundException(courseName);

        Team t = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );

        // Se il team non appartiene a quel corso lancia una eccezione
        if (!t.getCourse().equals(c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        // Se è già stata creata una configurazione per il team corrente lancia una eccezione
        if (vmConfigRepository.findById(teamId).isPresent())
            throw new VMConfigAlreadyExistException(courseName, teamId);

        VMConfig vmConfig = mapper.map(vmConfigDTO, VMConfig.class);
        vmConfig.setTeam(t);
        return mapper.map(vmConfigRepository.save(vmConfig), VMConfigDTO.class);
    }

    //@PreAuthorize("hasRole('PROFESSOR') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public VMConfigDTO updateVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se nessun modello è ancora stato settato lancia una eccezione
        if (c.getVmModel() == null)
            throw new VMModelNotFoundException(courseName);

        Team t = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );

        // Se il team non appartiene a quel corso lancia una eccezione
        if (!t.getCourse().equals(c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        // Se è già stata creata una configurazione per il team corrente lancia una eccezione
        VMConfig vmConfig = vmConfigRepository.findById(teamId).orElseThrow(
                () -> new VMConfigNotFoundException(courseName, teamId)
        );

        // Se le risorse settate sono minori di quelle già utilizzate, lancia una eccezione
        List<VMInstance> vms = t.getVms();
        int totalCpu = vms.stream().mapToInt(VMInstance::getCpu).sum();
        int totalRam = vms.stream().mapToInt(VMInstance::getRamSize).sum();
        int totalDisk = vms.stream().mapToInt(VMInstance::getDiskSize).sum();
        int totalVm = vms.size();
        int totalActive = (int)vms.stream().filter(VMInstance::isActive).count();
        if (totalCpu > vmConfigDTO.getMaxCpu() || totalRam > vmConfigDTO.getMaxRam() ||
                totalDisk > vmConfigDTO.getMaxDisk() || totalVm > vmConfigDTO.getMaxVm() ||
                totalActive > vmConfigDTO.getMaxActive())
            throw new VMResourcesAlreadyAllocatedException(teamId, totalCpu, vmConfigDTO.getMaxCpu(),
                    totalRam, vmConfigDTO.getMaxRam(), totalDisk, vmConfigDTO.getMaxDisk(),
                    totalActive, vmConfigDTO.getMaxActive(), totalVm, vmConfigDTO.getMaxVm());

        vmConfig.setMaxCpu(vmConfigDTO.getMaxCpu());
        vmConfig.setMaxDisk(vmConfigDTO.getMaxDisk());
        vmConfig.setMaxRam(vmConfigDTO.getMaxRam());
        vmConfig.setMaxActive(vmConfigDTO.getMaxActive());
        vmConfig.setMaxVm(vmConfigDTO.getMaxVm());

        return mapper.map(vmConfig, VMConfigDTO.class);
    }

    //@PreAuthorize("@securityApiAuth.isMe(#studentId)")
    @Override
    public VMInstanceDTO createVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, Long teamId, String studentId) {
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

        Team courseTeam = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );

        //Controllo se il team appartiene a quel corso
        if (!courseTeam.getCourse().equals(c))
            throw new TeamNotBelongToCourseException(courseTeam.getName(), courseName);

        //Controllo se lo studente appartiene al team
        if (!courseTeam.getMembers().contains(s))
            throw new StudentNotBelongToTeamException(studentId, teamId);

        //Controllo se è stato settato un modello di VM per il corso
        VMModel vmModel = vmModelRepository.findByIdIgnoreCase(courseName).orElseThrow(
                () -> new NoVMModelException(courseName)
        );

        //Controllo se è stata settata una configurazione per il team
        VMConfig config = courseTeam.getVmConfig();
        if (config == null)
            throw new NoVMConfigException(courseTeam.getName(), courseName);

        //Controllo delle specifiche settate
        Map<String, Integer> limits = config.config();
        Map<String, Integer> allocatedRes = courseTeam.getVms().stream().map(vm-> vm.config().entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
        Map<String, Integer> vmConfig = vmInstanceDTO.getConfig();

        List<VMInstance> instances = courseTeam.getVms();
        int totalVms = instances.size();

        if (totalVms >= config.getMaxVm())
            throw new VMNumberExceedException(courseTeam.getName(), courseName, config.getMaxVm());

        vmConfig.forEach((key, value) -> {
            int limit = limits.get(key);
            int allocated = allocatedRes.getOrDefault(key, 0);
            if (value + allocated > limit)
                throw new VMLimitExceedException(courseTeam.getName(), courseName, key, limit, value);
        });

        //Create VMInstance
        VMInstance vmInstance = mapper.map(vmInstanceDTO, VMInstance.class);
        vmInstance.setVmModel(vmModel);
        vmInstance.setTeam(courseTeam);
        vmInstance.addOwner(s);

        return mapper.map(vmInstanceRepository.save(vmInstance), VMInstanceDTO.class);
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

        //Se Ci sono già troppe VM attive, lancia una eccezione
        Team t = vmInstance.getTeam();

        int maxActive = t.getVmConfig().getMaxActive();
        int currentActive = 1 + (int)t.getVms().stream().filter(VMInstance::isActive).count();

        if (currentActive > maxActive)
            throw new VMActiveLimitExceededException(t.getId());

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

    @Override
    public VMModelDTO getVMModel(String courseName) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        VMModel vmModel = c.getVmModel();
        if(c.getVmModel() == null)
            throw new VMModelNotFoundException(courseName);

        return mapper.map(vmModel, VMModelDTO.class);
    }

    @Override
    public List<StudentDTO> getVMOwners(Long vmInstanceId) {
        return vmInstanceRepository.findById(vmInstanceId)
                .orElseThrow(
                        () -> new VMInstanceNotFoundException(vmInstanceId)
                )
                .getOwners().stream()
                .map(owner->mapper.map(owner, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public VMModelDTO getVMModelOfInstance(Long vmInstanceId) {
        VMInstance vmInstance =  vmInstanceRepository.findById(vmInstanceId)
                .orElseThrow(
                        () -> new VMInstanceNotFoundException(vmInstanceId)
                );
        return mapper.map(vmInstance, VMModelDTO.class);
    }

    @Override
    public List<VMInstanceDTO> getTeamVMs(Long teamId) {
        if (!teamRepository.findById(teamId).isPresent())
            throw new TeamNotFoundException(teamId);

        return vmInstanceRepository.findAllByTeamId(teamId).stream()
                .map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public VMConfigDTO getTeamConfig(String courseName, Long teamId) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        VMConfig config =  teamRepository.findById(teamId)
                .orElseThrow(
                        () -> new TeamNotFoundException(teamId)
                ).getVmConfig();

        if (!config.getTeam().getCourse().equals(c))
            throw new TeamNotBelongToCourseException();

        return mapper.map(config, VMConfigDTO.class);
    }

    @Override
    public List<VMInstanceDTO> getAllVms() {
        return vmInstanceRepository.findAll()
                .stream().map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<VMInstanceDTO> getActiveVms() {
        return vmInstanceRepository.findAllByActiveTrue()
                .stream().map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<VMInstanceDTO> getActiveTeamVms(Long teamId) {
        if (!teamRepository.findById(teamId).isPresent())
            throw new TeamNotFoundException(teamId);
        return vmInstanceRepository.findAllByTeamIdAndActiveTrue(teamId)
                .stream().map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<VMInstanceDTO> getOfflineVms() {
        return vmInstanceRepository.findAllByActiveFalse()
                .stream().map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<VMInstanceDTO> getOfflineTeamVms(Long teamId) {
        if (!teamRepository.findById(teamId).isPresent())
            throw new TeamNotFoundException(teamId);
        return vmInstanceRepository.findAllByTeamIdAndActiveFalse(teamId)
                .stream().map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }
}
