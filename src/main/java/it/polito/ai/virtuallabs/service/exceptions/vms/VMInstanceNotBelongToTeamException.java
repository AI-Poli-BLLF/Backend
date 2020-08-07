package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMInstanceNotBelongToTeamException extends VMServiceException {
    public VMInstanceNotBelongToTeamException(Long teamId, Long vmInstanceId) {
        super(String.format("VM instance %d doesn't belong to team %d", vmInstanceId, teamId));
    }
}
