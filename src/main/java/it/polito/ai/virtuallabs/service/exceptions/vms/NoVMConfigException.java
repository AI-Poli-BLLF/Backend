package it.polito.ai.virtuallabs.service.exceptions.vms;

public class NoVMConfigException extends VMServiceException {
    public NoVMConfigException(String teamName, String courseName) {
        super(String.format("No VM configuration for the course %s has been set for team %s yet",
                courseName, teamName));
    }
}
