package it.polito.ai.virtuallabs.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String store(MultipartFile image, String userId);
    byte[] getImage(String userId);
    String storeAssignmentImage(MultipartFile image, String professorId, Long assignmentId);
    byte[] getAssignmentImage(Long assignmentId);
    String storeDraftImage(MultipartFile image, Long draftId);
    byte[] getDraftImage(String studentId, String courseName, Long assignmentId, Long draftId);
}
