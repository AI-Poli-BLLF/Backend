package it.ai.polito.lab2.service.exceptions;

public class TeamAlreadyExistException extends TeamServiceException{
    public TeamAlreadyExistException() {super();}
    public TeamAlreadyExistException(String teamName, String courseName){
        super(String.format("The team <%s> already exist for the course <%s>", teamName, courseName));
    }
}
