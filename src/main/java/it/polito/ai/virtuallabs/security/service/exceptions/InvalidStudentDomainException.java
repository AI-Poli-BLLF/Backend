package it.polito.ai.virtuallabs.security.service.exceptions;

public class InvalidStudentDomainException extends UserServiceException {
    public InvalidStudentDomainException(String domain) {
        super(String.format("Wrong student Email domain. " +
                "Found: %s - " +
                "Expected: studenti.polito.it", domain));
    }
}
