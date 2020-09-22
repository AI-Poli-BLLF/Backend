package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMOsDTO;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMOs;

import java.util.List;

public interface VMService {
    VMModelDTO createVMModel(VMModelDTO vmModelDTO, String courseName);
    VMModelDTO updateVMModel(VMModelDTO vmModelDTO, String courseName);
    VMConfigDTO createVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName);
    VMConfigDTO updateVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName);
    VMInstanceDTO createVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, Long teamId, String studentId);
    VMInstanceDTO editVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, Long teamId, String studentId);
    void setVMOwners(String courseName, Long teamId, Long vmInstanceId, List<String> ownerIds);
    void bootVMInstance(String courseName, Long teamId, Long vmInstanceId);
    void shutdownVMInstance(String courseName, Long teamId, Long vmInstanceId);
    void deleteVMInstance(String courseName, Long teamId, Long vmInstanceId);

    VMModelDTO getCourseVMModel(String courseName);
    void deleteVmsByCourseName(String courseName);
    void deleteVmsByTeamId(Long teamId);
    List<StudentDTO> getVMOwners(String courseName, Long teamId, Long vmInstanceId);
    StudentDTO getVMCreator(String courseName, Long teamId, Long vmInstanceId);
    VMModelDTO getVMModelOfInstance(String courseName, Long teamId, Long vmInstanceId);
    List<VMInstanceDTO> getTeamVMs(String courseName, Long teamId);
    VMInstanceDTO getSingleTeamVm(String courseName, Long teamId, Long vmInstanceId);
    VMConfigDTO getTeamConfig(String courseName, Long teamId);
    List<VMInstanceDTO> getAllVms();
    List<VMInstanceDTO> getActiveVms();
    List<VMInstanceDTO> getActiveTeamVms(String courseName, Long teamId);
    List<VMInstanceDTO> getOfflineVms();
    List<VMInstanceDTO> getOfflineTeamVms(String courseName, Long teamId);

    VMOsDTO createVMOs(VMOsDTO vmOsDTO);
    void deleteVMOs(String osName);
    VMOsDTO addOsVersion(String osName, String version);
    void deleteOsVersion(String osName, String version);
    List<VMOsDTO> getAvailableVmOs();
    List<VMOsDTO> getAllVmOs();

    VMConfigDTO getUsedResources();
    VMConfigDTO getAllocatedResourcesByUsers();
    VMConfigDTO getAllocatedResourcesByConfig();
    }
