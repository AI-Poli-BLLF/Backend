package it.polito.ai.virtuallabs.service.exceptions;

public class StudentNotBelongToTeamException extends TeamServiceException {
    public StudentNotBelongToTeamException(String studentId, Long teamId) {
        super(String.format("Student %s doesn't belong to the team %d", studentId, teamId));
    }

    public StudentNotBelongToTeamException() {
        super();
    }
}
