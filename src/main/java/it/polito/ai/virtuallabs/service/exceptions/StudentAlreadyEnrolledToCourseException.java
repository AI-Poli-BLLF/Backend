package it.polito.ai.virtuallabs.service.exceptions;

public class StudentAlreadyEnrolledToCourseException extends TeamServiceException {
    public StudentAlreadyEnrolledToCourseException(String studentId, String courseName) {
        super(String.format("Student %s is already enrolled to course %s", studentId, courseName));
    }
}
