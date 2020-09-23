package it.polito.ai.virtuallabs.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String store(MultipartFile image, String userId);
    byte[] getImage(String userId);
    String storeAssignmentImage(MultipartFile image, String professorId, Long assignmentId);
    byte[] getAssignmentImage(Long assignmentId);
    String storeDraftImage(String studentId, String courseName, Long assignmentId, Long draftId, MultipartFile image);
    byte[] getDraftImage(String studentId, String courseName, Long assignmentId, Long draftId);
    String storeCorrectionImage(String professorId, String courseName, Long assignmentId, Long draftId, Long correctionId, MultipartFile image);
    byte[] getCorrectionImageForDraft(String studentId, String courseName, Long assignmentId, Long draftId);
}
