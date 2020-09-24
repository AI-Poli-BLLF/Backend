package it.polito.ai.virtuallabs.service.exceptions;

public class CourseNameEditException extends TeamServiceException {
    public CourseNameEditException() {
        super("An attempt to change the course Name of entity Course has been made");
    }
}
