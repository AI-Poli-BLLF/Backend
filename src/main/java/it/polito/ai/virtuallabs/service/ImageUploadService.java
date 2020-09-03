package it.polito.ai.virtuallabs.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String store(MultipartFile image, String userId);
    byte[] getImage(String userId);
    String storeAssignmentImage(MultipartFile image, String professorId, String assignmentId);
    byte[] getAssignmentImage(String assignmentId);
    String storeDraftImage(MultipartFile image, String draftId);
    byte[] getDraftImage(String draftId);
}
