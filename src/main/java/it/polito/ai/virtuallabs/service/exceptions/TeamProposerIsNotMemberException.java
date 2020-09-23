package it.polito.ai.virtuallabs.service.exceptions;

public class TeamProposerIsNotMemberException extends TeamServiceException {

    public TeamProposerIsNotMemberException(String proposerId) {
        super(String.format("Impossibile creare il gruppo: proponente non presente nei membri"));
        //super(String.format("Team proposer %s is not listed among the members", proposerId));
    }

}
