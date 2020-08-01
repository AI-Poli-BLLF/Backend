package it.polito.ai.virtuallabs.dtos.vms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class VMInstanceDTO {

    @EqualsAndHashCode.Include
    private Long id;

    private boolean active;

    @Min(value = 1)
    @EqualsAndHashCode.Include
    private int cpu;

    @Min(value = 1024)
    @EqualsAndHashCode.Include
    private int ramSize;

    @Min(value = 16)
    @EqualsAndHashCode.Include
    private int diskSize;

    public Map<String, Integer> getConfig(){
        Map<String,Integer> map = new HashMap<>();

        map.put("cpu", cpu);
        map.put("disk_size", diskSize);
        map.put("ram_size", ramSize);
        return map;
    }
}
