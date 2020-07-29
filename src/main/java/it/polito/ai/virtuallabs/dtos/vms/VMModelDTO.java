package it.polito.ai.virtuallabs.dtos.vms;

import it.polito.ai.virtuallabs.entities.vms.VMModel.OS;
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


    public VMModelDTO(OS os, String version) {
        this.os = os.toString();
        this.version = version;
    }

    public void setOs(OS os) {
        this.os = os.toString();
    }
}
