package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;

public interface VMService {
    void createVMModel(VMModelDTO vmModelDTO, String courseName);
    void setVMConfiguration(VMConfigDTO vmConfigDTO, Long teamId, String courseName);
}
