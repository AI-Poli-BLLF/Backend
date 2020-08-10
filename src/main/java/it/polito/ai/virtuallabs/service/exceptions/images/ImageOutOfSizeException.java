package it.polito.ai.virtuallabs.service.exceptions.images;

public class ImageOutOfSizeException extends ImageServiceException {
    public ImageOutOfSizeException(long size) {
        super(String.format("Image is too big.\nFound: %d MB\nExpected: 3 MB", size));
    }
}
