package it.ai.polito.lab2.service.exceptions;

public class TeamNotFoundException extends TeamServiceException{
    public TeamNotFoundException() {super();}
    public TeamNotFoundException(Long id) {
        super(String.format("Team <%d> doesn't exist", id));
    }
}
