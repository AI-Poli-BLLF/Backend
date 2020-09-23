package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class AssignmentNotOfThisCourseException extends AssignmentServiceException{
    public AssignmentNotOfThisCourseException(Long assignmentId, String courseName) {
        super(String.format("Assignment %d is not of the course %s", assignmentId, courseName));
    }
}
