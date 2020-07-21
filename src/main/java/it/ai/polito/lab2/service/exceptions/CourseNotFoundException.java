package it.ai.polito.lab2.service.exceptions;

public class CourseNotFoundException extends TeamServiceException{
    public CourseNotFoundException() {super();}
    public CourseNotFoundException(String courseName) {
        super(String.format("Course <%s> doesn't exist", courseName));
    }
}
