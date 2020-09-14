package it.polito.ai.virtuallabs.service.exceptions;

public class CourseAlreadyExistException extends TeamServiceException {
    public CourseAlreadyExistException(String courseName) {
        super(String.format("Course %s already exist", courseName));
    }
}
