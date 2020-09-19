package it.polito.ai.virtuallabs.service.exceptions;

public class ProfessorAlreadyOwnCourseException extends TeamServiceException {
    public ProfessorAlreadyOwnCourseException(String professorId, String courseName) {
        super(String.format("Professor %s already own course %s", professorId, courseName));
    }
}
