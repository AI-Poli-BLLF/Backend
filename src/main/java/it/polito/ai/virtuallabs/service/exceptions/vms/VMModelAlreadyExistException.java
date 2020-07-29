package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMModelAlreadyExistException extends VMServiceException {
    public VMModelAlreadyExistException(String courseName) {
        super(String.format("%s already has a VM model"));
    }
}
