package it.polito.ai.virtuallabs.service.exceptions;

public class TokenNotFoundException extends NotificationException {
    public TokenNotFoundException(Long teamId, String studentId) {
        super(String.format("Token not found for student <%s> in team <%d>", studentId, teamId));
    }

}
