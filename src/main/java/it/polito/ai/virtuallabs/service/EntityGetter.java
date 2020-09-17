package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.entities.*;
import it.polito.ai.virtuallabs.entities.tokens.RegistrationToken;
import it.polito.ai.virtuallabs.entities.tokens.Token;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.entities.vms.VMOs;

public interface EntityGetter {
    Course getCourse(String courseName);
    Team getTeam(Long teamId);
    VMModel getVMModel(String courseName);
    VMConfig getVMConfig(Long teamId);
    Student getStudent(String studentId);
    Professor getProfessor(String professorId);
    VMInstance getVMInstance(Long vmInstanceId);
    Assignment getAssignment(String assignmentId);
    Draft getDraft(String draftId);
    Token getToken(String tokenId);
    RegistrationToken getRegistrationToken(String tokenId);
    VMOs getVmOsVersion(String osName, String version);
    VMOs getVmOs(String osName);
}
