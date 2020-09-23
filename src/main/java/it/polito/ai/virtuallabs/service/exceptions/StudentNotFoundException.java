package it.polito.ai.virtuallabs.service.exceptions;

public class StudentNotFoundException extends TeamServiceException {
    public StudentNotFoundException() {super();}
    public StudentNotFoundException(String studentId) {
        super(String.format("Impossibile creare il gruppo: studente <%s> inesistente", studentId));
        //super(String.format("Student <%s> doesn't exist", studentId));
    }
}
