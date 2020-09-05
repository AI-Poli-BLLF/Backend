package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.AssignmentDTO;
import it.polito.ai.virtuallabs.dtos.DraftDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.entities.*;
import it.polito.ai.virtuallabs.repositories.*;
import it.polito.ai.virtuallabs.security.service.SecurityApiAuth;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.exceptions.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.nimbus.State;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {
    @Autowired
    ModelMapper mapper;
    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    ProfessorRepository professorRepository;
    @Autowired
    DraftRepository draftRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    StudentRepository studentRepository;

    @PreAuthorize("hasRole('PROFESSOR')")
    public boolean addAssignment(AssignmentDTO assignmentDTO, String courseId){
        if(assignmentRepository.findById(assignmentDTO.getId()).isPresent())
            return false;
        if(assignmentDTO.getExpiryDate().before(assignmentDTO.getReleaseDate()))
            return false;
        Assignment assignment = assignmentRepository.save(mapper.map(assignmentDTO, Assignment.class));
        String professorId = SecurityApiAuth.getPrincipal().getId();
        Professor professor = professorRepository.findByIdIgnoreCase(professorId)
                .orElseThrow(() -> new ProfessorNotFoundException(professorId));
        assert (professor.getAssignments().contains(assignment) || assignment.getProfessor() != null);
        assignment.setProfessor(professor);
        Course course = courseRepository.findByNameIgnoreCase(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        assignment.setCourse(course);
        return true;
    }

    public List<AssignmentDTO> getAssignments(){
        List<AssignmentDTO> assignments = assignmentRepository.findAll().stream()
                .map(p -> mapper.map(p, AssignmentDTO.class))
                .collect(Collectors.toList());
        return assignments;
    }

    @Override
    public List<AssignmentDTO> getAssignmentPerProfessorPerCourse(String professorId, String courseId) {
        return assignmentRepository.findAll()
                .stream()
                .filter(a -> a.getProfessor().getId().equals(professorId))
                .filter(a -> a.getCourse().getName().equals(courseId))
                .map(a -> mapper.map(a, AssignmentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AssignmentDTO> getAssignment(String assignmentId) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        return assignment.map(p -> mapper.map(p, AssignmentDTO.class));
    }

    @Override
    public ProfessorDTO getAssignmentProfessor(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new AssignmentNotFoundException(assignmentId)
        );
        Professor professor = assignment.getProfessor();
//        assignment = mapper.map(assignment, Assignment.class);
        return mapper.map(professor, ProfessorDTO.class);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT')")
    public boolean addDraft(DraftDTO draftDTO, String assignmentId, String studentId) {
        if(draftRepository.findById(draftDTO.getId()).isPresent())
            return false;
        Draft draft = draftRepository.save(mapper.map(draftDTO, Draft.class));
        Assignment assignment = assignmentRepository
                .findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        draft.setAssignment(assignment);
        Student student = studentRepository.findById(studentId).orElseThrow(
                () -> new StudentNotFoundException(studentId)
        );
        draft.setStudent(student);
        return true;
    }

    @Override
    public DraftDTO getDraft(String draftId) {
        DraftDTO draftDTO = mapper.map(draftRepository.findById(draftId).get(), DraftDTO.class);
        if(draftDTO.getId() == draftId)
            return draftDTO;
        else throw new DraftNotFoundException(draftId);
    }

    @Override
    public void setDraftStatus(String draftId, Draft.State state) {
        Draft draft = draftRepository.findById(draftId).orElseThrow(
                () -> new DraftNotFoundException(draftId)
        );
        Assignment assignment = draft.getAssignment();
        if(assignment.getExpiryDate().before(new Timestamp(System.currentTimeMillis())))
            throw new SubmitAfterExpiryException(draftId);
        draftRepository.findById(draftId)
                .ifPresent(d -> d.setStatus(state));
    }

    @Override
    public void passiveDraftSubmit() {
        draftRepository.findAll()
                .stream()
                .filter(d -> d.getAssignment().getExpiryDate().before(new Timestamp(System.currentTimeMillis())))
                .forEach(d -> d.setStatus(Draft.State.SUBMITTED));
    }
}
