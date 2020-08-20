package it.polito.ai.virtuallabs.security.service.exceptions;

public class InvalidProfessorDomainException extends UserServiceException {
    public InvalidProfessorDomainException(String domain) {
        super(String.format("Wrong professor Email domain. " +
                "Found: %s - " +
                "Expected: polito.it", domain));
    }
}
