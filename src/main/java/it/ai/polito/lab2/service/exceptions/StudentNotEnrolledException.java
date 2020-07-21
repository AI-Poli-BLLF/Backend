package it.ai.polito.lab2.service.exceptions;

public class StudentNotEnrolledException extends TeamServiceException{
    public StudentNotEnrolledException() {super();}
    public StudentNotEnrolledException(String studentId, String courseName) {
        super(String.format("Student <%s> is not enrolled to the course <%s>", studentId, courseName));
    }
}
