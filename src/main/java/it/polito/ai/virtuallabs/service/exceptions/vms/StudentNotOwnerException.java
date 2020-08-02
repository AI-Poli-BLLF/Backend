package it.polito.ai.virtuallabs.service.exceptions.vms;

public class StudentNotOwnerException extends VMServiceException {
    public StudentNotOwnerException(String studentId, Long vmInstanceId) {
        super(String.format("Student %s is not an owner of the vm %d", studentId, vmInstanceId));
    }
}
