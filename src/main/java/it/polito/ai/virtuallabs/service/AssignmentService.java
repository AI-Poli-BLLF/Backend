package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.*;

import java.util.List;

public interface AssignmentService {

    AssignmentDTO addAssignment(AssignmentDTO assignmentDTO, String courseId);
    List<AssignmentDTO> getAssignmentsOfCourse(String courseId);

    DraftDTO readAssigment(Long assignmentId, String studentId, String courseName);

    List<DraftDTO> getDrafts(String courseName, Long assignmentId);
    StudentDTO getStudentForDraft(String courseName, Long assignmentId, Long draftId);
    List<DraftDTO> getDraftsForStudent(String studentId, String courseName, Long assignmentId);

    CorrectionDTO correctDraft(String courseName, Long assignmentId, Long draftId, int grade);

    void passiveDraftSubmit();

    void deleteAssignmentAndDraftsByCourseName(String courseName);


    DraftDTO submitDraft(String studentId, String courseName, Long assignmentId);
}

