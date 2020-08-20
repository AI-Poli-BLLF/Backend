package it.polito.ai.virtuallabs.security.service.exceptions;

public class UserAlreadyExistException extends UserServiceException {
    public UserAlreadyExistException() {
        super("User already exist");
    }
}
