package it.ai.polito.lab2.service.exceptions;

public class InvalidOrExpiredTokenException extends NotificationException{
    public InvalidOrExpiredTokenException(String token) {
        super(String.format("Token <%s> doesn't exist or has expired", token));
    }
}
