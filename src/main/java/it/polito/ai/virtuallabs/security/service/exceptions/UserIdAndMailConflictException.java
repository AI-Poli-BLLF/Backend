package it.polito.ai.virtuallabs.security.service.exceptions;

public class UserIdAndMailConflictException extends UserServiceException {
    public UserIdAndMailConflictException(String id, String idFromMail) {
        super(String.format("User Id and and user id got from mail are different: " +
                "%s - %s", id, idFromMail));
    }
}
