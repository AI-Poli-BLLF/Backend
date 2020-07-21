package it.ai.polito.lab2.security.exceptions;

public class UnbindedUserException extends RuntimeException {
    public UnbindedUserException(String message) {
        super(message);
    }
}
