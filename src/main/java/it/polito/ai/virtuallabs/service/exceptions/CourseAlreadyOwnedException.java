package it.polito.ai.virtuallabs.service.exceptions;

public class CourseAlreadyOwnedException extends TeamServiceException {
    public CourseAlreadyOwnedException(String courseName) {
        super(String.format("Course <%s> is already assigned to a professor", courseName));
    }
}
