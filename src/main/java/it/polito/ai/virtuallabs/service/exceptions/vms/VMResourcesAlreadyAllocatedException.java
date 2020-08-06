package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMResourcesAlreadyAllocatedException extends VMServiceException {
    public VMResourcesAlreadyAllocatedException(Long teamId, int totalCpu, int maxCpu, int totalRam,
                                                int maxRam, int totalDisk, int maxDisk,
                                                int totalActive, int maxActive, int totalVm, int maxVm) {
        super(String.format("Some resources are already allocated for the theam %d and cannot be reduced:\n" +
                "CPU: current %d, set %d\n" +
                "RAM: current %d, set %d\n" +
                "DISK: current %d, set %d" +
                "ACTIVE: current %d, set %d" +
                "TOTAL VM: current %d, set %d",
                teamId, totalCpu, maxCpu, totalRam, maxRam, totalDisk, maxDisk,
                totalActive, maxActive, totalVm, maxVm));
    }
}
