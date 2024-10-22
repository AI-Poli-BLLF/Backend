package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMOsDTO;
import it.polito.ai.virtuallabs.service.VMService;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/API/vm-os")
public class AdminController {

    @Autowired
    private VMService vmService;

    @GetMapping({"","/"})
    private List<VMOsDTO> getAvailableVmOs(){
        try {
            return vmService.getAvailableVmOs();
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/all")
    private List<VMOsDTO> getAllVmOs(){
        try {
            return vmService.getAllVmOs();
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/resources/used")
    private VMConfigDTO getUsedResources(){
        try {
            return vmService.getUsedResources();
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/resources/reserved")
    private VMConfigDTO getReservedResources(){
        try {
            return vmService.getAllocatedResourcesByConfig();
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/resources/allocated")
    private VMConfigDTO getAllocatedResources(){
        try {
            return vmService.getAllocatedResourcesByUsers();
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping({"","/"})
    @ResponseStatus(value = HttpStatus.CREATED)
    private VMOsDTO createVmOs(@RequestBody @Valid VMOsDTO vmOsDTO){
        try {
            return vmService.createVMOs(vmOsDTO);
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping("/{osName}")
    private void deleteOs(@PathVariable String osName){
        try {
            vmService.deleteVMOs(osName);
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/{osName}")
    @ResponseStatus(value = HttpStatus.CREATED)
    private VMOsDTO addOsVersion(@PathVariable String osName ,@RequestBody String version){
        try {
            return vmService.addOsVersion(osName, version);
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping("/{osName}/{version}")
    private void deleteOsVersion(@PathVariable String osName, @PathVariable String version){
        try {
            vmService.deleteOsVersion(osName, version);
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
