package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMModelNotFoundException extends VMServiceException {
    public VMModelNotFoundException(String courseName) {
        super(String.format("Course %s hasn't VMModel yet. Please call createVMModel() first", courseName));
    }
}
