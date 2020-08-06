package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;

import java.util.List;

public interface VMService {
    VMModelDTO createVMModel(VMModelDTO vmModelDTO, String courseName);
    VMModelDTO updateVMModel(VMModelDTO vmModelDTO, String courseName);
    VMConfigDTO createVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName);
    VMConfigDTO updateVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName);
    VMInstanceDTO createVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, Long teamId, String studentId);
    void shareVMOwnership(Long vmInstanceId, String ownerId, String teammateId);
    void bootVMInstance(Long vmInstanceId, String ownerId);
    void shutdownVMInstance(Long vmInstanceId, String ownerId);
    void deleteVMInstance(Long vmInstanceId, String ownerId);

    VMModelDTO getVMModel(String courseName);
    List<StudentDTO> getVMOwners(Long vmInstanceId);
    VMModelDTO getVMModelOfInstance(Long vmInstanceId);
    List<VMInstanceDTO> getTeamVMs(Long teamId);
    VMConfigDTO getTeamConfig(String courseName, Long teamId);
    List<VMInstanceDTO> getAllVms();
    List<VMInstanceDTO> getActiveVms();
    List<VMInstanceDTO> getActiveTeamVms(Long teamId);
    List<VMInstanceDTO> getOfflineVms();
    List<VMInstanceDTO> getOfflineTeamVms(Long teamId);
}
