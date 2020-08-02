package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMInstanceNotFoundException extends VMServiceException {
    public VMInstanceNotFoundException(Long vmInstanceId) {
        super(String.format("VM instance with id %d not found", vmInstanceId));
    }
}
