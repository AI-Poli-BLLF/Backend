package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMServiceException extends RuntimeException{
    public VMServiceException() {
        super();
    }

    public VMServiceException(String message) {
        super(message);
    }

    public VMServiceException(Throwable cause) {
        super(cause);
    }
}
