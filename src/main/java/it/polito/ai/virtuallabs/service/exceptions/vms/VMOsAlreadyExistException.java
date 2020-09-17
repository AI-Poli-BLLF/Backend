package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMOsAlreadyExistException extends VMServiceException {
    public VMOsAlreadyExistException(String osName) {
        super(String.format("Os %s already exist", osName));
    }

    public VMOsAlreadyExistException(String osName, String version) {
        super(String.format("Os %s already has version %s", osName, version));
    }
}
