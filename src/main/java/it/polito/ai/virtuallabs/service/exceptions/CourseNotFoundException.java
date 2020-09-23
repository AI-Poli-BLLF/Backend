package it.polito.ai.virtuallabs.service.exceptions;

public class CourseNotFoundException extends TeamServiceException{
    public CourseNotFoundException() {super();}
    public CourseNotFoundException(String courseName) {
        super(String.format("Impossibile creare il gruppo: corso inesistente"));
        //super(String.format("Course <%s> doesn't exist", courseName));
    }
}
