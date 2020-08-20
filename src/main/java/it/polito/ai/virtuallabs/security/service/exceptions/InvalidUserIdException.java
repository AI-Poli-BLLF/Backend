package it.polito.ai.virtuallabs.security.service.exceptions;

public class InvalidUserIdException extends UserServiceException {
    public InvalidUserIdException(String id) {
        super(String.format("Invalid user id " +
                "Found: %s - " +
                "Expected: s<six numbers>", id));
    }
}
