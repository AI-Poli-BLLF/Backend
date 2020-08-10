package it.polito.ai.virtuallabs.service.exceptions.images;

public class ImageServiceException extends RuntimeException{
    public ImageServiceException() {
        super();
    }

    public ImageServiceException(String message) {
        super(message);
    }

    public ImageServiceException(Throwable cause) {
        super(cause);
    }
}
