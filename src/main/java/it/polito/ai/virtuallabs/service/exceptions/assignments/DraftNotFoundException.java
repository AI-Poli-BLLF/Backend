package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class DraftNotFoundException extends RuntimeException{
    public DraftNotFoundException(Long draftId){
        super(String.format("Draft <%s> not found", draftId));
    }
}
