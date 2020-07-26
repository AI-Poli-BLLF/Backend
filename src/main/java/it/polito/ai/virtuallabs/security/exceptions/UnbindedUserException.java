package it.polito.ai.virtuallabs.security.exceptions;

public class UnbindedUserException extends RuntimeException {
    public UnbindedUserException(String message) {
        super(message);
    }
}
