package it.polito.ai.virtuallabs.dtos.vms;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class VMOsDTO {
    private Long id;
    private String osName;
    private Set<String> versions = new HashSet<>();
}
