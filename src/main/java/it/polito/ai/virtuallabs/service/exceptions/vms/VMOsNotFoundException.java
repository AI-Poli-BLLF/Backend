package it.polito.ai.virtuallabs.service.exceptions.vms;

public class VMOsNotFoundException extends VMServiceException {
    public VMOsNotFoundException(String os) {
        super(String.format("Os not found %s", os));
    }
    public VMOsNotFoundException(String os, String version) {
        super(String.format("Os %s doesn't have version %s", os, version));
    }
}
