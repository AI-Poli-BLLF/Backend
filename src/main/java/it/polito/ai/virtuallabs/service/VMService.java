package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;

public interface VMService {
    void createVMModel(VMModelDTO vmModelDTO, String courseName);
    void setVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName);
    void createVMInstance(VMInstanceDTO vmInstanceDTO, String courseName, String studentId);
    void shareVMOwnership(Long vmInstanceId, String ownerId, String teammateId);
    void bootVMInstance(Long vmInstanceId, String ownerId);
    void shutdownVMInstance(Long vmInstanceId, String ownerId);
    void deleteVMInstance(Long vmInstanceId, String ownerId);
}
