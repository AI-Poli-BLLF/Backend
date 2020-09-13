package it.polito.ai.virtuallabs.service.exceptions.vms;

public class StudentAlreadyOwnVm extends VMServiceException {
    public StudentAlreadyOwnVm(String ownerId, Long vmInstanceId) {
        super(String.format("Student %s already own vm %d", ownerId, vmInstanceId));
    }
}
