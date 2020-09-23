package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class DraftNotFoundException extends AssignmentServiceException{
    public DraftNotFoundException(Long draftId){
        super(String.format("Draft <%s> not found", draftId));
    }
}
