package it.polito.ai.virtuallabs.service.exceptions.images;

public class ImageExtensionUnsupported extends ImageServiceException {
    public ImageExtensionUnsupported(String foundExtension) {
        super(String.format("Unsupported image extension. Found: %s",foundExtension));
    }
}
