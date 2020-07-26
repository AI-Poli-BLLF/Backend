package it.polito.ai.virtuallabs.service.exceptions;

public class NotificationException extends RuntimeException{

    public NotificationException() { super(); }

    public NotificationException(String message) { super(message); }

    public NotificationException(Throwable cause) { super(cause); }
}
