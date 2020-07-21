package it.ai.polito.lab2.service.exceptions;

public class StudentAlreadyBelongsToTeam extends TeamServiceException{
    public StudentAlreadyBelongsToTeam() {super();}
    public StudentAlreadyBelongsToTeam(String studentId, String courseName) {
        super(String.format("Student <%s> already belongs to a team of the course <%s>", studentId, courseName));
    }
}
