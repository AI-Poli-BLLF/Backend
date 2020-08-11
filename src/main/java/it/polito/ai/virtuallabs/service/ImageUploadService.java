package it.polito.ai.virtuallabs.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String store(MultipartFile image, String userId);
    byte[] getImage(String userId);
}
