package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMConfigNotFoundException extends VMServiceException {
    public VMConfigNotFoundException(String courseName, Long teamId) {
        super(String.format("VM config not found for the team %d of the course %s",
                teamId, courseName));
    }

    public VMConfigNotFoundException(Long teamId) {
        super(String.format("VM config not found for the team %d",
                teamId));
    }
}
