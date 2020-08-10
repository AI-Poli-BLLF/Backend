package it.polito.ai.virtuallabs.service.exceptions.images;

public class ImageStorageException extends ImageServiceException {
    public ImageStorageException(String imageName) {
        super(String.format("Could not store image %s. Please try again!", imageName));
    }
}
