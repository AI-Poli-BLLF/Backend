package it.polito.ai.virtuallabs.service.exceptions;

public class NoTeamException extends TeamServiceException {
    public NoTeamException(String studentId, String courseName) {
        super(String.format("Student %s doesn't belong to a team of course %s yet",
                studentId, courseName));
    }
}
