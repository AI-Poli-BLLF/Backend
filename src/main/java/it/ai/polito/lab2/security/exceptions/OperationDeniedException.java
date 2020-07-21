package it.ai.polito.lab2.security.exceptions;

public class OperationDeniedException extends RuntimeException {
    public OperationDeniedException(String message) {
        super(message);
    }
}
