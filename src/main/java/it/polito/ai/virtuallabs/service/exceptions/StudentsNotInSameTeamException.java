package it.polito.ai.virtuallabs.service.exceptions;

public class StudentsNotInSameTeamException extends TeamServiceException {
    public StudentsNotInSameTeamException(String studentId1, String studentId2, String teamName) {
        super(String.format("Students %s and %s are not in the same team (%s)",
                studentId1, studentId2, teamName));
    }
    public StudentsNotInSameTeamException(){
        super("Students are not in the same team");
    }
}
