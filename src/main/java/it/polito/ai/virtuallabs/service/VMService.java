package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;

import java.util.List;

public interface VMService {
    void createVMModel(VMModelDTO vmModelDTO, String courseName);
    void setVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName);
    void createVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, String studentId);
    void shareVMOwnership(Long vmInstanceId, String ownerId, String teammateId);
    void bootVMInstance(Long vmInstanceId, String ownerId);
    void shutdownVMInstance(Long vmInstanceId, String ownerId);
    void deleteVMInstance(Long vmInstanceId, String ownerId);

    List<StudentDTO> getVMOwners(Long vmInstanceId);
    VMModelDTO getVMModelOfInstance(Long vmInstanceId);
    List<VMInstanceDTO> getTeamVMs(Long teamId);
    VMConfigDTO getTeamConfig(Long teamId);
    List<VMInstanceDTO> getAllVms();
    List<VMInstanceDTO> getActiveVms();
    List<VMInstanceDTO> getActiveTeamVms(Long teamId);
    List<VMInstanceDTO> getOfflineVms();
    List<VMInstanceDTO> getOfflineTeamVms(Long teamId);
}
