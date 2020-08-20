package it.polito.ai.virtuallabs.security.service.exceptions;

public class UserServiceException extends RuntimeException {
    public UserServiceException() {
        super();
    }

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(Throwable cause) {
        super(cause);
    }
}
