package it.ai.polito.lab2.service.exceptions;

public class CourseNotEnabledException extends TeamServiceException{
    public CourseNotEnabledException() {super();}
    public CourseNotEnabledException(String courseName) {
        super(String.format("Course <%s> isn't enabled", courseName));
    }
}
