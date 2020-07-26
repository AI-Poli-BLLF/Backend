package it.polito.ai.virtuallabs.service.exceptions;

public class InconsistentStudentDataException extends TeamServiceException{
    public InconsistentStudentDataException() {super("Inconsistent data has been inserted in your csv file");}
}
