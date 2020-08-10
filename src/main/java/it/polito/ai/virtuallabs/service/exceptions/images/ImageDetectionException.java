package it.polito.ai.virtuallabs.service.exceptions.images;

public class ImageDetectionException extends ImageServiceException{
    public ImageDetectionException() {
        super("Error during image detection. Please try again");
    }
}
