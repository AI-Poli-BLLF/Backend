package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMActiveLimitExceededException extends VMServiceException {
    public VMActiveLimitExceededException(Long teamId) {
        super(String.format("Team %d has too much vms active", teamId));
    }
}
