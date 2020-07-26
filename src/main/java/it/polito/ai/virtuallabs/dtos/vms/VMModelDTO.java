package it.polito.ai.virtuallabs.dtos.vms;

import it.polito.ai.virtuallabs.entities.vms.VMModel.OS;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class VMModelDTO {
    @NotEmpty
    private String id;
    @NotEmpty
    private String os;
    @NotEmpty
    private String version;


    public VMModelDTO(String id, OS os, String version) {
        this.id = id;
        this.os = os.toString();
        this.version = version;
    }

    public void setOs(OS os) {
        this.os = os.toString();
    }
}
