package it.polito.ai.virtuallabs.service.exceptions;

public class CourseNotEnabledException extends TeamServiceException{
    public CourseNotEnabledException() {super();}
    public CourseNotEnabledException(String courseName) {
        super(String.format("Course <%s> isn't enabled", courseName));
    }
}
