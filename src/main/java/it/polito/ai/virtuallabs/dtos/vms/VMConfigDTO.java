package it.polito.ai.virtuallabs.dtos.vms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@Data
public class VMConfigDTO {
    private Long id;
    @Min(value = 1)
    private int maxCpu;
    @Min(value = 16)
    private int maxDisk;
    @Min(value = 1024)
    private int maxRam;
    @Min(1)
    private int maxActive;
    @Min(1)
    private int maxVm;

    public VMConfigDTO(int maxCpu, int maxDisk, int maxRam, int maxActive, int maxVm) {
        this.maxCpu = maxCpu;
        this.maxDisk = maxDisk;
        this.maxRam = maxRam;
        this.maxActive = maxActive;
        this.maxVm = maxVm;
    }
}
