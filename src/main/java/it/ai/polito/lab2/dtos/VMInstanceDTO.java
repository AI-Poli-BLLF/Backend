package it.ai.polito.lab2.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class VMInstanceDTO {

    @EqualsAndHashCode.Include
    private Long id;
    private boolean online;

    @EqualsAndHashCode.Include
    private int cpu;

    @EqualsAndHashCode.Include
    private int ramSize;

    @EqualsAndHashCode.Include
    private int diskSize;
}
