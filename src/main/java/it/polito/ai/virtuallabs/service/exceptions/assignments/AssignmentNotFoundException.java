package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class AssignmentNotFoundException extends AssignmentServiceException {
    public AssignmentNotFoundException(Long id) {
        super(String.format("Assignment <%s> does not exist", id));
    }
}
