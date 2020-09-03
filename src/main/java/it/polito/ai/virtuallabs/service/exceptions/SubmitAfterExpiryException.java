package it.polito.ai.virtuallabs.service.exceptions;

public class SubmitAfterExpiryException extends RuntimeException {
    public SubmitAfterExpiryException(String id){
        super(String.format("Too late for manual submission of <%s>, last version has been saved as submitted", id));
    }
}
