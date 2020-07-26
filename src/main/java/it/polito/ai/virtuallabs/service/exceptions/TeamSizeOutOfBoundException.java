package it.polito.ai.virtuallabs.service.exceptions;

public class TeamSizeOutOfBoundException extends TeamServiceException {
    public TeamSizeOutOfBoundException() {super();}
    public TeamSizeOutOfBoundException(int min, int max) {
        super(String.format("Team size must be between %d and %d", min, max));
    }
}
