package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.AssignmentDTO;
import it.polito.ai.virtuallabs.dtos.DraftDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.entities.Draft;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    AssignmentDTO addAssignment(AssignmentDTO assignmentDTO, String courseId);
    List<AssignmentDTO> getAssignments();
    List<AssignmentDTO> getAssignmentPerProfessorPerCourse(String professorId, String courseId);
    Optional<AssignmentDTO> getAssignment(Long assignmentId);
    List<AssignmentDTO> getAssignmentsForCourse(String courseName);
    ProfessorDTO getAssignmentProfessor(Long assignmentId);

    boolean addDraft(DraftDTO draftDTO, Long assignmentId, String studentId);
    DraftDTO getDraft(Long draftId);
    List<DraftDTO> getDrafts(Long assignmentId);
    StudentDTO getStudentForDraft(Long draftId);
    List<DraftDTO> getDraftsForStudent(String studentId);

    void setDraftStatus(Long draftId, Draft.State state);

    void passiveDraftSubmit();

    void deleteAssignmentAndDraftsByCourseName(String courseName);

    void setDraftLock(Long draftId);
    void setDraftUnlock(Long draftId);
}

