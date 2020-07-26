package it.polito.ai.virtuallabs.security.exceptions;

public class OperationDeniedException extends RuntimeException {
    public OperationDeniedException(String message) {
        super(message);
    }
}
