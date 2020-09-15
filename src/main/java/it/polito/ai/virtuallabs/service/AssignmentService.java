package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.AssignmentDTO;
import it.polito.ai.virtuallabs.dtos.DraftDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.entities.Draft;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    boolean addAssignment(AssignmentDTO assignmentDTO, String courseId);
    List<AssignmentDTO> getAssignments();
    List<AssignmentDTO> getAssignmentPerProfessorPerCourse(String professorId, String courseId);
    Optional<AssignmentDTO> getAssignment(String assignmentId);
    ProfessorDTO getAssignmentProfessor(String assignmentId);

    boolean addDraft(DraftDTO draftDTO, String assignmentId, String studentId);
    DraftDTO getDraft(String draftId);

    void setDraftStatus(String draftId, Draft.State state);

    void passiveDraftSubmit();

    void deleteAssignmentAndDraftsByCourseName(String courseName);
}

