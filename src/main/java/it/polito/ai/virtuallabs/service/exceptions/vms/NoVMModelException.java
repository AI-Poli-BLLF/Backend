package it.polito.ai.virtuallabs.service.exceptions.vms;

public class NoVMModelException extends VMServiceException {
    public NoVMModelException(String courseName) {
        super(String.format("No VM Model has been set for the course %s", courseName));
    }
}
