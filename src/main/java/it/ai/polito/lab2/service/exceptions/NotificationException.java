package it.ai.polito.lab2.service.exceptions;

public class NotificationException extends RuntimeException{

    public NotificationException() { super(); }

    public NotificationException(String message) { super(message); }

    public NotificationException(Throwable cause) { super(cause); }
}
