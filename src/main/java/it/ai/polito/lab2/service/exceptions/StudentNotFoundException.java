package it.ai.polito.lab2.service.exceptions;

public class StudentNotFoundException extends TeamServiceException {
    public StudentNotFoundException() {super();}
    public StudentNotFoundException(String studentId) {
        super(String.format("Student <%s> doesn't exist", studentId));
    }
}
