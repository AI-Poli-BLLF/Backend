package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMOsDTO;
import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.entities.vms.VMOs;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMConfigRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMInstanceRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMModelRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMOsRepository;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.VMService;
import it.polito.ai.virtuallabs.service.exceptions.*;
import it.polito.ai.virtuallabs.service.exceptions.vms.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VMServiceImpl implements VMService {

    @Autowired
    private VMModelRepository vmModelRepository;
    @Autowired
    private VMConfigRepository vmConfigRepository;
    @Autowired
    private VMInstanceRepository vmInstanceRepository;
    @Autowired
    private VMOsRepository vmOsRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityGetter getter;

    @PreAuthorize("@securityApiAuth.ownCourse(#courseName)")
    @Override
    public VMModelDTO createVMModel(VMModelDTO vmModelDTO, String courseName) {
        Course c = getter.getCourse(courseName);

        //Se è già stata creata un VMModel associato al corso lancia una eccezione
        if (vmModelRepository.findByIdIgnoreCase(courseName).isPresent() || c.getVmModel() != null)
            throw new VMModelAlreadyExistException(courseName);

        VMOs vmOs = getter.getVmOsVersion(vmModelDTO.getOs(), vmModelDTO.getVersion());
        VMModel vmModel = new VMModel();
        vmModel.setOs(vmOs);
        vmModel.setVersion(vmModelDTO.getVersion());
        vmModel.setCourse(c);
        c.setVmModel(vmModel);
        return mapper.map(vmModelRepository.save(vmModel), VMModelDTO.class);
    }

    @Override
    public VMModelDTO updateVMModel(VMModelDTO vmModelDTO, String courseName) {
        Course c = getter.getCourse(courseName);

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se è già stata creata un VMModel associato al corso lancia una eccezione
        VMModel vmModel = getter.getVMModel(courseName);
        VMOs vmOs = getter.getVmOsVersion(vmModelDTO.getOs(), vmModelDTO.getVersion());
        vmModel.setOs(vmOs);
        vmModel.setVersion(vmModelDTO.getVersion());

        vmModel = vmModelRepository.save(vmModel);
        vmModelDTO.setId(vmModel.getId());
        return vmModelDTO;
    }

    // todo: preauth
    //@PreAuthorize("hasRole('PROFESSOR') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public VMConfigDTO createVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName) {
        Course c = getter.getCourse(courseName);

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se nessun modello è ancora stato settato lancia una eccezione
        if (c.getVmModel() == null)
            throw new VMModelNotFoundException(courseName);

        Team t = getter.getTeam(teamId);

        // Se il team non appartiene a quel corso lancia una eccezione
        if (!t.getCourse().equals(c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        // Se è già stata creata una configurazione per il team corrente lancia una eccezione
        if (vmConfigRepository.findById(teamId).isPresent())
            throw new VMConfigAlreadyExistException(courseName, teamId);

        VMConfig vmConfig = mapper.map(vmConfigDTO, VMConfig.class);
        vmConfig.setTeam(t);
        t.setVmConfig(vmConfig);
        return mapper.map(vmConfigRepository.save(vmConfig), VMConfigDTO.class);
    }

    //@PreAuthorize("hasRole('PROFESSOR') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public VMConfigDTO updateVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName) {
        Course c = getter.getCourse(courseName);

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se nessun modello è ancora stato settato lancia una eccezione
        if (c.getVmModel() == null)
            throw new VMModelNotFoundException(courseName);

        Team t = getter.getTeam(teamId);

        // Se il team non appartiene a quel corso lancia una eccezione
        if (!t.getCourse().equals(c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        // Se è già stata creata una configurazione per il team corrente lancia una eccezione
        VMConfig vmConfig = getter.getVMConfig(teamId);

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

    @PreAuthorize("@securityApiAuth.isMe(#studentId)")
    @Override
    public VMInstanceDTO createVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, Long teamId, String studentId) {
        Course c = getter.getCourse(courseName);

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        Student s = getter.getStudent(studentId);

        //Controllo se lo studente è iscritto al corso
        Set<String> enrolledCourses = studentRepository.getCourseNames(studentId)
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
        if (!enrolledCourses.contains(courseName.toLowerCase()))
            throw new StudentNotEnrolledException(studentId, courseName);

        Team courseTeam = getter.getTeam(teamId);

        //Controllo se il team appartiene a quel corso
        if (!teamBelongToCourse(courseTeam, c))
            throw new TeamNotBelongToCourseException(courseTeam.getName(), courseName);

        //Controllo se lo studente appartiene al team
        if (!courseTeam.getMembers().contains(s))
            throw new StudentNotBelongToTeamException(studentId, teamId);

        //Controllo se è stato settato un modello di VM per il corso
        VMModel vmModel = getter.getVMModel(courseName);
        if(vmModel == null)
            throw new NoVMModelException(courseName);

        //Controllo se è stata settata una configurazione per il team
        VMConfig config = courseTeam.getVmConfig();
        if (config == null)
            throw new NoVMConfigException(courseTeam.getName(), courseName);

        //Controllo delle specifiche settate
        Map<String, Integer> limits = config.config();
        Map<String, Integer> allocatedRes = courseTeam.getVms().stream().map(vm-> vm.config().entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
        Map<String, Integer> vmConfig = vmInstanceDTO.config();

        List<VMInstance> instances = courseTeam.getVms();
        int totalVms = instances.size();

        if (totalVms+1 > config.getMaxVm())
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
        vmInstance.setCreator(s);

        return mapper.map(vmInstanceRepository.save(vmInstance), VMInstanceDTO.class);
    }

    // todo: controllare correttezza
    @PreAuthorize("@securityApiAuth.isMe(#studentId)")
    @Override
    public VMInstanceDTO editVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, Long teamId, String studentId) {
        Course c = getter.getCourse(courseName);

        //Se il corso non è attivo lancia una eccezione
        if (!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        VMInstance vmInstance = getter.getVMInstance(vmInstanceDTO.getId());

        Student s = getter.getStudent(studentId);

        //Controllo se lo studente è iscritto al corso
        Set<String> enrolledCourses = studentRepository.getCourseNames(studentId)
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
        if (!enrolledCourses.contains(courseName.toLowerCase()))
            throw new StudentNotEnrolledException(studentId, courseName);

        //Controllo se lo studente è owner della vm
        if(!vmInstance.getOwners().contains(s))
            throw new StudentNotOwnerException(studentId, vmInstance.getId());

        Team courseTeam = getter.getTeam(teamId);

        //Controllo se il team appartiene a quel corso
        if (!teamBelongToCourse(courseTeam, c))
            throw new TeamNotBelongToCourseException(courseTeam.getName(), courseName);

        //Controllo se lo studente appartiene al team
        if (!courseTeam.getMembers().contains(s))
            throw new StudentNotBelongToTeamException(studentId, teamId);

        //Controllo se è stato settato un modello di VM per il corso
        VMModel vmModel = getter.getVMModel(courseName);
        if(vmModel == null)
            throw new NoVMModelException(courseName);

        //Controllo se è stata settata una configurazione per il team
        VMConfig config = courseTeam.getVmConfig();
        if (config == null)
            throw new NoVMConfigException(courseTeam.getName(), courseName);

        //Controllo delle specifiche settate
        Map<String, Integer> limits = config.config();
        Map<String, Integer> allocatedRes = courseTeam.getVms().stream().map(vm-> vm.config().entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
        Map<String, Integer> vmConfig = vmInstanceDTO.config();
        Map<String, Integer> vmPrevConfig = vmInstance.config();

        vmConfig.forEach((key, value) -> {
            int limit = limits.get(key);
            int alreadyUsed = vmPrevConfig.get(key);
            int allocated = allocatedRes.getOrDefault(key, 0);
            if (value + allocated > limit + alreadyUsed)
                throw new VMLimitExceedException(courseTeam.getName(), courseName, key, limit, value);
        });

        vmInstance.setCpu(vmInstanceDTO.getCpu());
        vmInstance.setDiskSize(vmInstanceDTO.getDiskSize());
        vmInstance.setRamSize(vmInstanceDTO.getRamSize());

        return mapper.map(vmInstance, VMInstanceDTO.class);
    }

    @PreAuthorize("@securityApiAuth.ownVm(#vmInstanceId)")
    @Override
    public void setVMOwners(String courseName, Long teamId, Long vmInstanceId, List<String> ownerIds){
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        VMInstance vm = getter.getVMInstance(vmInstanceId);

        Set<Student> owners = ownerIds.stream().map(o -> getter.getStudent(o)).collect(Collectors.toSet());

        //Controllo che il team appartenga al corso
        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), c.getName());

        //Controllo che il corso sia attivo
        if(!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Controllo che la VM appartiene al team
        if(!vm.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);

        //Controllo che non ci siano membri duplicati
        if(ownerIds.size() != owners.size())
            throw new DuplicateStudentException();

        owners.remove(vm.getCreator());
        vm.setOwners(new ArrayList<>(owners));
    }

    @PreAuthorize("@securityApiAuth.ownVm(#vmInstanceId)")
    @Override
    public void bootVMInstance(String courseName, Long teamId, Long vmInstanceId) {
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        VMInstance vmInstance = getter.getVMInstance(vmInstanceId);

        if (!vmInstance.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);

        //Se Ci sono già troppe VM attive, lancia una eccezione
        int maxActive = t.getVmConfig().getMaxActive();
        int currentActive = 1 + (int)t.getVms().stream().filter(VMInstance::isActive).count();

        if (currentActive > maxActive)
            throw new VMActiveLimitExceededException(t.getId());

        vmInstance.setActive(true);
    }

    @PreAuthorize("@securityApiAuth.ownVm(#vmInstanceId)")
    @Override
    public void shutdownVMInstance(String courseName, Long teamId, Long vmInstanceId) {
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        VMInstance vmInstance = getter.getVMInstance(vmInstanceId);

        if (!vmInstance.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);


        vmInstance.setActive(false);
    }

    @PreAuthorize("@securityApiAuth.ownVm(#vmInstanceId)")
    @Override
    public void deleteVMInstance(String courseName, Long teamId, Long vmInstanceId) {
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        VMInstance vmInstance = getter.getVMInstance(vmInstanceId);

        if (!vmInstance.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);

        /*List<Student> owners = vmInstance.getOwners();

        owners.forEach(vmInstance::removeOwner);
        vmInstance.getTeam().getVms().remove(vmInstance);
        vmInstance.getVmModel().getVmInstances().remove(vmInstance);*/

        vmInstanceRepository.delete(vmInstance);
    }

    @Override
    public VMModelDTO getCourseVMModel(String courseName) {
        Course c = getter.getCourse(courseName);

        VMModel vmModel = c.getVmModel();
        if(c.getVmModel() == null)
            throw new VMModelNotFoundException(courseName);

        return mapper.map(vmModel, VMModelDTO.class);
    }

    @Override
    public List<StudentDTO> getVMOwners(String courseName, Long teamId, Long vmInstanceId) {
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        VMInstance v = getter.getVMInstance(vmInstanceId);

        if (!v.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);

        return v.getOwners().stream()
                .map(owner->mapper.map(owner, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO getVMCreator(String courseName, Long teamId, Long vmInstanceId){
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        VMInstance vm = getter.getVMInstance(vmInstanceId);

        //Controllo che il team appartenga al corso
        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), c.getName());

        //Controllo che la VM appartiene al team
        if(!vm.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);

        return mapper.map(vm.getCreator(), StudentDTO.class);
    }

    @Override
    public VMModelDTO getVMModelOfInstance(String courseName, Long teamId, Long vmInstanceId) {
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        VMInstance vmInstance =  getter.getVMInstance(vmInstanceId);

        if (!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        if(!vmInstance.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);

        return mapper.map(vmInstance.getVmModel(), VMModelDTO.class);
    }

    @Override
    public List<VMInstanceDTO> getTeamVMs(String courseName, Long teamId) {
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        if (!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        return vmInstanceRepository.findAllByTeamId(teamId).stream()
                .map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public VMInstanceDTO getSingleTeamVm(String courseName, Long teamId, Long vmInstanceId) {
        Course c = getter.getCourse(courseName);

        Team t = getter.getTeam(teamId);

        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        VMInstance v = getter.getVMInstance(vmInstanceId);

        if (!v.getTeam().equals(t))
            throw new VMInstanceNotBelongToTeamException(teamId, vmInstanceId);

        return mapper.map(v, VMInstanceDTO.class);
    }

    @Override
    public VMConfigDTO getTeamConfig(String courseName, Long teamId) {
        Course c = getter.getCourse(courseName);

        VMConfig config =  getter.getVMConfig(teamId);

        if (!teamBelongToCourse(config.getTeam(), c))
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
    public List<VMInstanceDTO> getActiveTeamVms(String courseName, Long teamId) {
        Course c = getter.getCourse(courseName);
        Team t = getter.getTeam(teamId);

        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

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
    public List<VMInstanceDTO> getOfflineTeamVms(String courseName, Long teamId) {
        Course c = getter.getCourse(courseName);
        Team t = getter.getTeam(teamId);

        if(!teamBelongToCourse(t, c))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        return vmInstanceRepository.findAllByTeamIdAndActiveFalse(teamId)
                .stream().map(vm-> mapper.map(vm, VMInstanceDTO.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("@securityApiAuth.ownCourse(#courseName)")
    @Override
    public void deleteVmsByCourseName(String courseName){
        vmInstanceRepository.deleteByTeamCourseNameIgnoreCase(courseName);
        vmConfigRepository.deleteByTeamCourseNameIgnoreCase(courseName);
        vmModelRepository.deleteByIdIgnoreCase(courseName);
    }

    private boolean teamBelongToCourse(Team t, Course c){
        return t.getCourse().equals(c);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public VMOsDTO createVMOs(VMOsDTO vmOsDTO) {
        if(vmOsRepository.findByOsNameIgnoreCase(vmOsDTO.getOsName()).isPresent())
            throw new VMOsAlreadyExistException(vmOsDTO.getOsName());

        VMOs vmOs = mapper.map(vmOsDTO, VMOs.class);
        vmOs = vmOsRepository.save(vmOs);
        vmOsDTO.setId(vmOs.getId());
        return vmOsDTO;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteVMOs(String osName) {
        vmOsRepository.deleteByOsNameIgnoreCase(osName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public VMOsDTO addOsVersion(String osName, String version) {
        VMOs vmOs = getter.getVmOs(osName);
        if(vmOs.getVersions().contains(version))
            throw new VMOsAlreadyExistException(osName, version);

        vmOs.addVersion(version);
        return mapper.map(vmOs, VMOsDTO.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteOsVersion(String osName, String version) {
        VMOs vmOs = getter.getVmOs(osName);
        vmOs.removeVersion(version);
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @Override
    public List<VMOsDTO> getAvailableVmOs() {
        return vmOsRepository.findAll().stream()
                .filter(vmOs -> !vmOs.getVersions().isEmpty())
                .map(vmOs -> mapper.map(vmOs, VMOsDTO.class))
                .collect(Collectors.toList());
    }
}
