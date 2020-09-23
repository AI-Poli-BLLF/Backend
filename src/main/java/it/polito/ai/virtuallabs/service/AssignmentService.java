package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.entities.Draft;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    AssignmentDTO addAssignment(String professorId, AssignmentDTO assignmentDTO, String courseId);
    List<AssignmentDTO> getAssignments();
    List<AssignmentDTO> getAssignmentPerProfessorPerCourse(String professorId, String courseId);
    Optional<AssignmentDTO> getAssignment(Long assignmentId);
    List<AssignmentDTO> getAssignmentsForCourse(String courseName);
    ProfessorDTO getAssignmentProfessor(Long assignmentId);

//    boolean addDraft(DraftDTO draftDTO, Long assignmentId, String studentId);
    DraftDTO readAssigment(Long assignmentId, String studentId, String courseName);
    boolean addDraft(DraftDTO draftDTO, Long assignmentId, String studentId);
    DraftDTO getDraft(Long draftId);
    List<DraftDTO> getDrafts(String professorId, String courseName, Long assignmentId);
    StudentDTO getStudentForDraft(String professorId, String courseName, Long assignmentId, Long draftId);
    List<DraftDTO> getDraftsForStudent(String studentId, String courseName, Long assignmentId);

    CorrectionDTO correctDraft(String professorId, String courseName, Long assignmentId, Long draftId, int grade);
    void setDraftStatus(Long draftId, Draft.DraftState state);

    void passiveDraftSubmit();

    void deleteAssignmentAndDraftsByCourseName(String courseName);

    void setDraftLock(Long draftId);
    void setDraftUnlock(Long draftId);

    int evaluateDraft(String professorId, String courseName, Long assignmentId, Long draftId, int grade);

    DraftDTO submitDraft(String studentId, String courseName, Long assignmentId);
}

