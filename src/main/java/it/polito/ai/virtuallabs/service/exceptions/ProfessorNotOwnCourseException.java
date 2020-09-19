package it.polito.ai.virtuallabs.service.exceptions;

public class ProfessorNotOwnCourseException extends TeamServiceException {
    public ProfessorNotOwnCourseException(String professorId, String courseName) {
        super(String.format("Professor %s doesn't own the course %s", professorId, courseName));
    }
}
