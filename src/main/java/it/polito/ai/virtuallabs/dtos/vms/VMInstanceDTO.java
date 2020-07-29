package it.polito.ai.virtuallabs.dtos.vms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class VMInstanceDTO {

    @EqualsAndHashCode.Include
    private Long id;

    private boolean online;

    @Min(value = 1)
    @EqualsAndHashCode.Include
    private int cpu;

    @Min(value = 1024)
    @EqualsAndHashCode.Include
    private int ramSize;

    @Min(value = 16)
    @EqualsAndHashCode.Include
    private int diskSize;
}
