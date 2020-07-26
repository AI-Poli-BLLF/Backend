package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;

public interface ManagementService {
    boolean createStudentUser(StudentDTO studentDTO);
    boolean createProfessorUser(ProfessorDTO professorDTO);
}
