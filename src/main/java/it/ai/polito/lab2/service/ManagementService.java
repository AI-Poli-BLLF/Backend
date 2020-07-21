package it.ai.polito.lab2.service;

import it.ai.polito.lab2.dtos.ProfessorDTO;
import it.ai.polito.lab2.dtos.StudentDTO;

public interface ManagementService {
    boolean createStudentUser(StudentDTO studentDTO);
    boolean createProfessorUser(ProfessorDTO professorDTO);
}
