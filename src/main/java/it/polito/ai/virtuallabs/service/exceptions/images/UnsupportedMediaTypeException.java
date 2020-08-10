package it.polito.ai.virtuallabs.service.exceptions.images;

public class UnsupportedMediaTypeException extends ImageServiceException {
    public UnsupportedMediaTypeException(String type){
        super(String.format("Unsupported media type: %s", type));
    }
}
