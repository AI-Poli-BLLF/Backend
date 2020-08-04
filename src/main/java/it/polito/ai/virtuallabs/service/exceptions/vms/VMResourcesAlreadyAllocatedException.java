package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMResourcesAlreadyAllocatedException extends VMServiceException {
    public VMResourcesAlreadyAllocatedException(Long teamId, int totalCpu, int maxCpu, int totalRam, int maxRam, int totalDisk, int maxDisk) {
        super(String.format("Some resources are already allocated for the theam %d and cannot be reduced:\n" +
                "CPU: current %d, set %d\n" +
                "RAM: current %d, set %d\n" +
                "DISK: current %d, set %d", teamId, totalCpu, maxCpu, totalRam, maxRam, totalDisk, maxDisk));
    }
}
