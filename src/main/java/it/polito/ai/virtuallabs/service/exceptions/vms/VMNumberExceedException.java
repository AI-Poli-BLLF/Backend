package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMNumberExceedException extends VMServiceException {
    public VMNumberExceedException(String teamName, String courseName, int maxVM) {
        super(String.format("The team %s of the course %s has already %d VM instances",
                teamName, courseName, maxVM));
    }
}
