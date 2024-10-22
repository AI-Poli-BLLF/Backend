package it.polito.ai.virtuallabs.service.exceptions;

public class ProfessorNotFoundException extends TeamServiceException {
    public ProfessorNotFoundException(String id) {
        super(String.format("Professor with id <%s> doesn't exist", id));
    }
}
