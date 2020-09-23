package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class AssignmentNotOfThisDraftException extends AssignmentServiceException{
    public AssignmentNotOfThisDraftException(Long assignmentId, Long draftId) {
        super(String.format("Draft %d is not for the assignment %d", draftId, assignmentId));
    }
}
