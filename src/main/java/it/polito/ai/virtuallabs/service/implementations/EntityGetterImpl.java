package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.Professor;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.repositories.CourseRepository;
import it.polito.ai.virtuallabs.repositories.ProfessorRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.repositories.TeamRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMConfigRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMInstanceRepository;
import it.polito.ai.virtuallabs.repositories.vms.VMModelRepository;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.exceptions.CourseNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.ProfessorNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.StudentNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.TeamNotFoundException;
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
}
