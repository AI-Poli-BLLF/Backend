package it.polito.ai.virtuallabs.service.exceptions;

public class AssignmentNotFoundException extends RuntimeException {
    public AssignmentNotFoundException(String id) {
        super(String.format("Assignment <%s> does not exist", id));
    }
}
