package it.polito.ai.virtuallabs.dtos.vms;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;


@Data
@NoArgsConstructor
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

    public Map<String, Integer> config(){
        Map<String,Integer> map = new HashMap<>();

        map.put("cpu", maxCpu);
        map.put("disk_size", maxDisk);
        map.put("ram_size", maxRam);
        map.put("active", maxActive);
        map.put("max_vm", maxVm);
        return map;
    }
}
