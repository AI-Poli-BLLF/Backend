package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMConfigAlreadyExistException extends VMServiceException {
    public VMConfigAlreadyExistException(String courseName, Long teamId) {
        super(String.format("VM configuration already exist for the team %d of the course %s",
                teamId, courseName));
    }
}
