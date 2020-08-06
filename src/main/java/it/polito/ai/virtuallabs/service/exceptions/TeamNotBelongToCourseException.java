package it.polito.ai.virtuallabs.service.exceptions;

public class TeamNotBelongToCourseException extends TeamServiceException {
    public TeamNotBelongToCourseException(){super();}
    public TeamNotBelongToCourseException(String teamName, String courseName) {
        super(String.format("%s doesn't belong to %s course", teamName, courseName));
    }
}
