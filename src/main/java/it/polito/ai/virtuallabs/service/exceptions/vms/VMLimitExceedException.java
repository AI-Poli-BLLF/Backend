package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMLimitExceedException extends VMServiceException {
    public VMLimitExceedException(String teamName, String courseName, String paramName, int limit, int current) {
        super(String.format("%s limit has been crossed: max is %d, current was %d\nTeam %s, Course %s",
                paramName, limit, current, teamName, courseName));
    }
}
