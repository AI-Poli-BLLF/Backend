package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class DraftNotLastSubmittedException extends AssignmentServiceException {
    public DraftNotLastSubmittedException(Long draftId, String studentId) {
        super(String.format("Draft %d is not the last submitted by student %s", draftId, studentId));
    }
}
