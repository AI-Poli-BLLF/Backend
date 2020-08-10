package it.polito.ai.virtuallabs.dtos.images;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageModel {
    private String name;
    private String type;
    private byte[] picByte;
}
