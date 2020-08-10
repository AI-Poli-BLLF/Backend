package it.polito.ai.virtuallabs.service.exceptions.images;

public class ImageConversionException extends ImageServiceException {
    public ImageConversionException(){
        super("Error during image conversion");
    }
}
