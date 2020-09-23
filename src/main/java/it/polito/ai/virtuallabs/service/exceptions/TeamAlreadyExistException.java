package it.polito.ai.virtuallabs.service.exceptions;

public class TeamAlreadyExistException extends TeamServiceException{
    public TeamAlreadyExistException() {super();}
    public TeamAlreadyExistException(String teamName, String courseName){
        super(String.format("Impossibile creare il gruppo: Nome gruppo duplicato"));
        //super(String.format("The team <%s> already exist for the course <%s>", teamName, courseName));
    }
}
