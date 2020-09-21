package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class AssignmentServiceException extends RuntimeException{
    public AssignmentServiceException() { super(); }
    public AssignmentServiceException(String message) { super(message); }
    public AssignmentServiceException(Throwable cause) { super(cause); }
}
