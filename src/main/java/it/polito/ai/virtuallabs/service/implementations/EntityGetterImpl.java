package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.entities.*;
import it.polito.ai.virtuallabs.entities.tokens.NotificationToken;
import it.polito.ai.virtuallabs.entities.tokens.RegistrationToken;
import it.polito.ai.virtuallabs.entities.tokens.Token;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.entities.vms.VMOs;
import it.polito.ai.virtuallabs.repositories.*;
import it.polito.ai.virtuallabs.repositories.tokens.NotificationTokenRepository;
import it.polito.ai.virtuallabs.repositories.tokens.RegistrationTokenRepository;
import it.polito.ai.virtuallabs.repositories.tokens.TokenRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMConfigRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMInstanceRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMModelRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMOsRepository;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.exceptions.*;
import it.polito.ai.virtuallabs.service.exceptions.assignments.AssignmentNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.DraftNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMConfigNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMInstanceNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMModelNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMOsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;

@Service
@Transactional
public class EntityGetterImpl implements EntityGetter {
    @Autowired
    private VMModelRepository vmModelRepository;
    @Autowired
    private VMConfigRepository vmConfigRepository;
    @Autowired
    private VMInstanceRepository vmInstanceRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private DraftRepository draftRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;
    @Autowired
    private VMOsRepository vmOsRepository;
    @Autowired
    private NotificationTokenRepository notificationTokenRepository;

    @Override
    public Course getCourse(String courseName){
        return courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );
    }

    @Override
    public Team getTeam(Long teamId){
        return teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );
    }

    @Override
    public VMModel getVMModel(String courseName){
        return vmModelRepository.findByIdIgnoreCase(courseName).orElseThrow(
                () -> new VMModelNotFoundException(courseName)
        );
    }

    @Override
    public VMConfig getVMConfig(Long teamId){
        return vmConfigRepository.findById(teamId).orElseThrow(
                () -> new VMConfigNotFoundException(teamId)
        );
    }

    @Override
    public Student getStudent(String studentId){
        return studentRepository.findByIdIgnoreCase(studentId).orElseThrow(
                () -> new StudentNotFoundException(studentId)
        );
    }

    @Override
    public Professor getProfessor(String professorId) {
        return professorRepository.findByIdIgnoreCase(professorId).orElseThrow(
                () -> new ProfessorNotFoundException(professorId)
        );
    }

    @Override
    public VMInstance getVMInstance(Long vmInstanceId){
        return vmInstanceRepository.findById(vmInstanceId).orElseThrow(
                () -> new VMInstanceNotFoundException(vmInstanceId)
        );
    }

    @Override
    public Assignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new AssignmentNotFoundException(assignmentId)
        );
    }

    @Override
    public Draft getDraft(Long draftId) {
        return draftRepository.findById(draftId).orElseThrow(
                () -> new DraftNotFoundException(draftId)
        );
    }

    @Override
    public Token getToken(String tokenId) {
        return tokenRepository.findById(tokenId).orElseThrow(
                () -> new InvalidOrExpiredTokenException(tokenId)
        );
    }

    @Override
    public RegistrationToken getRegistrationToken(String tokenId) {
        return registrationTokenRepository.findById(tokenId).orElseThrow(
                () -> new InvalidOrExpiredTokenException(tokenId)
        );
    }

    @Override
    public VMOs getVmOsVersion(String osName, String version){
        VMOs vmOs =  vmOsRepository.findByOsNameIgnoreCase(osName).orElseThrow(
                () -> new VMOsNotFoundException(osName)
        );
        if(!vmOs.getVersions().contains(version))
            throw new VMOsNotFoundException(osName, version);

        return vmOs;
    }

    @Override
    public VMOs getVmOs(String osName) {
        return vmOsRepository.findByOsNameIgnoreCase(osName).orElseThrow(
                () -> new VMOsNotFoundException(osName)
        );
    }

    @Override
    public NotificationToken getNotificationToken(String tokenId){
        return notificationTokenRepository.findById(tokenId).orElseThrow(
                () -> new InvalidOrExpiredTokenException(tokenId)
        );
    }
}
