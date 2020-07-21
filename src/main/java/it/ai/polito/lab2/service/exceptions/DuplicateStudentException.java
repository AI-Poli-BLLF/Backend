package it.ai.polito.lab2.service.exceptions;

public class DuplicateStudentException extends TeamServiceException{
    public DuplicateStudentException() {
        super();
    }

    public DuplicateStudentException(int duplicate){
        super(String.format("Found %d duplicate students", duplicate));
    }
}
