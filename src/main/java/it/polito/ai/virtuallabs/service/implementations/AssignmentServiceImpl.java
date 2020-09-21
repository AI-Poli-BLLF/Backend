package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.AssignmentDTO;
import it.polito.ai.virtuallabs.dtos.DraftDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.entities.*;
import it.polito.ai.virtuallabs.repositories.*;
import it.polito.ai.virtuallabs.security.service.SecurityApiAuth;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.exceptions.*;
import it.polito.ai.virtuallabs.service.exceptions.assignments.AssignmentNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.AssignmentServiceException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.DraftNotFoundException;
import org.hibernate.id.Assigned;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
    public AssignmentDTO addAssignment(AssignmentDTO assignmentDTO, String courseId){

        if(assignmentDTO.getId() != null) {
            if (assignmentRepository.findById(assignmentDTO.getId()).isPresent())
                throw new AssignmentServiceException();
        }

         Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new CourseNotFoundException(courseId)
        );

        if(assignmentDTO.getExpiryDate().before(assignmentDTO.getReleaseDate()))
            throw new AssignmentServiceException("expiryBeforeRelease");

        Assignment assignment = mapper.map(assignmentDTO, Assignment.class);
        String professorId = SecurityApiAuth.getPrincipal().getId();
        Professor professor = professorRepository.findByIdIgnoreCase(professorId)
                .orElseThrow(() -> new ProfessorNotFoundException(professorId));
        assert (professor.getAssignments().contains(assignment) || assignment.getProfessor() != null);

        assignment.setProfessor(professor);
        assignment.setCourse(course);
        Assignment assignment1 = assignmentRepository.save(assignment);


        DraftDTO draftDTO = DraftDTO.builder()
                .state(DraftDTO.State.NULL)
                .grade(0)
                .locker(false)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        course.getStudents()
                .forEach(s -> addDraft(draftDTO, assignment1.getId(), s.getId()));

        return mapper.map(assignment1, AssignmentDTO.class);
    }
    private DraftDTO addFDraft(DraftDTO draftDTO, Assignment assignment, Student student){
        Draft draft = mapper.map(draftDTO, Draft.class);
        draft.setStudent(student);
        draft.setAssignment(assignment);
        return mapper.map(draftRepository.save(draft), DraftDTO.class);
    }

    public List<AssignmentDTO> getAssignments(){
        List<AssignmentDTO> assignments = assignmentRepository.findAll().stream()
                .map(p -> mapper.map(p, AssignmentDTO.class))
                .collect(Collectors.toList());
        return assignments;
    }

    @Override
    public List<AssignmentDTO> getAssignmentsForCourse(String courseName) {
        return assignmentRepository.findAll()
                .stream()
//                .map(a -> mapper.map(a, Assignment.class))
                .filter(a -> a.getCourse().getName().equals(courseName))
                .map(a -> mapper.map(a, AssignmentDTO.class))
                .collect(Collectors.toList());
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
    public Optional<AssignmentDTO> getAssignment(Long assignmentId) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        return assignment.map(p -> mapper.map(p, AssignmentDTO.class));
    }

    @Override
    public ProfessorDTO getAssignmentProfessor(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new AssignmentNotFoundException(assignmentId)
        );
        Professor professor = assignment.getProfessor();
//        assignment = mapper.map(assignment, Assignment.class);
        return mapper.map(professor, ProfessorDTO.class);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT')")
    public boolean addDraft(DraftDTO draftDTO, Long assignmentId, String studentId) {
        if(draftDTO.getId() != null) {
            if (draftRepository.findById(draftDTO.getId()).isPresent())
                return false;
        }
        Draft draft = mapper.map(draftDTO, Draft.class);
        Assignment assignment = assignmentRepository
                .findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        Student student = studentRepository.findById(studentId).orElseThrow(
                () -> new StudentNotFoundException(studentId)
        );

        Draft draft1 = draftRepository.save(mapper.map(draftDTO, Draft.class));
        draft1.setAssignment(assignment);
        draft1.setStudent(student);

        return true;
    }

    @Override
    public DraftDTO getDraft(Long draftId) {
        DraftDTO draftDTO = mapper.map(draftRepository.findById(draftId).get(), DraftDTO.class);
        if(draftDTO.getId().equals(draftId))
            return draftDTO;
        else throw new DraftNotFoundException(draftId);
    }

    @Override
    public List<DraftDTO> getDrafts(Long assignmentId){
        Assignment assignment = this.assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new AssignmentNotFoundException(assignmentId)
        );
        return assignment.getDrafts().stream()
                .map(a -> mapper.map(a, DraftDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void setDraftStatus(Long draftId, Draft.State state) {
        Draft draft = draftRepository.findById(draftId).orElseThrow(
                () -> new DraftNotFoundException(draftId)
        );
        Assignment assignment = draft.getAssignment();
        if(assignment.getExpiryDate().before(new Timestamp(System.currentTimeMillis())))
            throw new SubmitAfterExpiryException(draftId.toString());
        draftRepository.findById(draftId)
                .ifPresent(d -> d.setStatus(state));
    }

    @Override
    public void passiveDraftSubmit() {
        draftRepository.findAll()
                .stream()
                .filter(d -> d.getAssignment()
                        .getExpiryDate()
                        .before(new Timestamp(System.currentTimeMillis())))
                .forEach(d -> d.setStatus(Draft.State.SUBMITTED));
    }

    @Override
    public void deleteAssignmentAndDraftsByCourseName(String courseName) {
        draftRepository.deleteByAssignmentCourseNameIgnoreCase(courseName);
        assignmentRepository.deleteByCourseNameIgnoreCase(courseName);
    }

    @Override
    public StudentDTO getStudentForDraft(Long draftId) {
        Student student = draftRepository.findById(draftId).orElseThrow(
                () -> new DraftNotFoundException(draftId)
        ).getStudent();
        return mapper.map(student, StudentDTO.class);
    }

    @Override
    public List<DraftDTO> getDraftsForStudent(String studentId) {
        return draftRepository.findAll()
                .stream()
                .filter(d -> d.getStudent().getId().equals(studentId))
                .map(d -> mapper.map(d, DraftDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void setDraftLock(Long draftId) {
        try {
            Draft draft = draftRepository.findById(draftId).orElseThrow(
                    () -> new DraftNotFoundException(draftId)
            );
            draft.setLocker(true);
        }catch (NoSuchElementException e){
            throw new DraftNotFoundException(draftId);
        }
    }

    @Override
    public void setDraftUnlock(Long draftId) {
        Draft draft = this.draftRepository.findById(draftId).orElseThrow(
                () -> new DraftNotFoundException(draftId)
        );
        draft.setLocker(false);
    }
}
