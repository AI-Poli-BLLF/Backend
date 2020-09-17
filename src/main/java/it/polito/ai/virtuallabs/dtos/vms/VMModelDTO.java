package it.polito.ai.virtuallabs.dtos.vms;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class VMModelDTO {

    private String id;
    @NotEmpty
    private String os;
    @NotEmpty
    private String version;


    public VMModelDTO(String os, String version) {
        this.os = os;
        this.version = version;
    }
}
