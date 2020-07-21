package it.ai.polito.lab2.service.exceptions;

public class TeamServiceException extends RuntimeException {
    public TeamServiceException() {
        super();
    }

    public TeamServiceException(String message) {
        super(message);
    }

    public TeamServiceException(Throwable cause) {
        super(cause);
    }
}
