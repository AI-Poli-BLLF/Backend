package it.polito.ai.virtuallabs.dtos.vms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
public class VMConfigDTO {
    @NotNull
    private Long id;
    @NotNull
    private int maxCpu;
    @NotNull
    private int maxDisk;
    @NotNull
    private int maxRam;
    @NotNull
    private int maxActive;
    @NotNull
    private int maxVm;
}
