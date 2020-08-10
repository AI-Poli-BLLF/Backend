package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.images.ImageModel;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String store(MultipartFile image, String userId);
    ImageModel getImage(String userId);
}
