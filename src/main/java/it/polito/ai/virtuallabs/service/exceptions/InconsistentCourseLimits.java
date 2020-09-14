package it.polito.ai.virtuallabs.service.exceptions;

public class InconsistentCourseLimits extends TeamServiceException {
    public InconsistentCourseLimits(int min, int max) {
        super(String.format("Min parameter <%d> is greater than max parameter <%d>", min, max));
    }
}
