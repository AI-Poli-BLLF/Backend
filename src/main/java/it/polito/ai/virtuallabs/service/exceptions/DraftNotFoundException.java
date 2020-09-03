package it.polito.ai.virtuallabs.service.exceptions;

public class DraftNotFoundException extends RuntimeException{
    public DraftNotFoundException(String draftId){
        super(String.format("Draft <%s> not found", draftId));
    }
}
