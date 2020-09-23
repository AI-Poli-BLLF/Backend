package it.polito.ai.virtuallabs.service.exceptions.assignments;

public class CorrectionNotFoundException extends AssignmentServiceException{
        public CorrectionNotFoundException(Long id) {
            super(String.format("Correction <%s> does not exist", id));
        }
}
