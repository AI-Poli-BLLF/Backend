package it.polito.ai.virtuallabs.dtos;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CorrectionDTO {
    private Long id;
    private String photoName;
    private Timestamp timestamp;
}
