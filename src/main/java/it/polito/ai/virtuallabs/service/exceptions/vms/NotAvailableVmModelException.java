package it.polito.ai.virtuallabs.service.exceptions.vms;

public class NotAvailableVmModelException extends VMServiceException {
    public NotAvailableVmModelException() {
        super("No Vm Models available for courses");
    }
}
