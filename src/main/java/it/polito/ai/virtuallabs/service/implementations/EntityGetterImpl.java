package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.entities.*;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.repositories.*;
import it.polito.ai.virtuallabs.repositories.vms.VMConfigRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMInstanceRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMModelRepository;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.exceptions.*;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMConfigNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMInstanceNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMModelNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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
    public Assignment getAssignment(String assignmentId) {
        return assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new AssignmentNotFoundException(assignmentId)
        );
    }

    @Override
    public Draft getDraft(String draftId) {
        return draftRepository.findById(draftId).orElseThrow(
                () -> new DraftNotFoundException(draftId)
        );
    }
}
