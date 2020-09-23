package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.entities.*;
import it.polito.ai.virtuallabs.repositories.*;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.exceptions.StudentNotEnrolledException;
import it.polito.ai.virtuallabs.service.exceptions.StudentNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.SubmitAfterExpiryException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.LinkedList;
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
    @Autowired
    CorrectionRepository correctionRepository;
    @Autowired
    EntityGetter entityGetter;

    @PreAuthorize("@securityApiAuth.isMe(#professorId) && @securityApiAuth.ownCourse(#courseId)")
    public AssignmentDTO addAssignment(String professorId, AssignmentDTO assignmentDTO, String courseId){
        assignmentDTO.setId(null);

         Course course = entityGetter.getCourse(courseId);

        if(assignmentDTO.getExpiryDate().before(assignmentDTO.getReleaseDate()))
            throw new AssignmentServiceException("expiryBeforeRelease");

        Assignment assignment = mapper.map(assignmentDTO, Assignment.class);
        Professor professor = entityGetter.getProfessor(professorId);

        assert (professor.getAssignments().contains(assignment) || assignment.getProfessor() != null);

        assignment.setProfessor(professor);
        assignment.setCourse(course);
        Assignment assignment1 = assignmentRepository.save(assignment);

        /*DraftDTO draftDTO = DraftDTO.builder()
                .state(DraftDTO.State.NULL)
                .grade(0)
                .locker(false)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        course.getStudents()
                .forEach(s -> addDraft(draftDTO, assignment1.getId(), s.getId()));*/

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
    @PreAuthorize("@securityApiAuth.isMe(#studentId)")
    public DraftDTO readAssigment(Long assignmentId, String studentId, String courseName) {
        Student student = entityGetter.getStudent(studentId);
        Course c = entityGetter.getCourse(courseName);

        if(!c.getStudents().contains(student))
            throw new StudentNotEnrolledException(studentId, courseName);

        Assignment assignment = entityGetter.getAssignment(assignmentId);
        List<Draft> drafts = draftRepository.findByAssignmentAndStudent(assignment, student);
        Draft anyRead = drafts.stream().filter(d -> d.getState().equals(Draft.DraftState.READ)).findFirst().orElse(null);
        if(anyRead != null){
            return mapper.map(anyRead, DraftDTO.class);
        }

        Draft draft = new Draft(Draft.DraftState.READ, assignment, student);

        draft = draftRepository.save(draft);
        return mapper.map(draft, DraftDTO.class);
    }

//    // todo: lo studente deve appartenere al corso nel preauth
//    @Override
//    @PreAuthorize("hasRole('STUDENT')")
//    public DraftDTO addDraft(MultipartFile image, Long assignmentId, String studentId);
//        Student student = entityGetter.getStudent(studentId);
//        Assignment assignment = entityGetter.getAssignment(assignmentId);
//        List<Draft> drafts = draftRepository.findByAssignmentAndStudent(assignment, student);
//        if(drafts.stream().noneMatch(d -> d.getState().equals(Draft.State.READ)) ||
//                drafts.stream().anyMatch(d -> d.getState().equals(Draft.State.SUBMITTED)
//        )){
//            // todo: da implementare una eccezione nel caso non sia stata letta la consegna o sia stato consegnato
//            throw new RuntimeException();
//        }
//
//        Draft draft = drafts.stream()
//                .filter(d -> d.getState().equals(Draft.State.READ)).min((draft1, draft2) -> draft1.getTimestamp().compareTo(draft2.getTimestamp())).get();
//
//        Draft draft1 = Draft.builder()
//                .locker(false)
//                .photoName(draft.getPhotoName())
//                .student(student)
//                .state(Draft.State.SUBMITTED)
//                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
//                .assignment(assignment)
//                .build();
//        draft1 = draftRepository.save(draft1);
//        return mapper.map(draft1, DraftDTO.class);
//    }

    @Override
    public DraftDTO getDraft(Long draftId) {
        DraftDTO draftDTO = mapper.map(draftRepository.findById(draftId).get(), DraftDTO.class);
        if(draftDTO.getId().equals(draftId))
            return draftDTO;
        else throw new DraftNotFoundException(draftId);
    }

    @PreAuthorize("@securityApiAuth.isMe(#professorId) && @securityApiAuth.ownCourse(#courseName)")
    @Override
    public List<DraftDTO> getDrafts(String professorId, String courseName, Long assignmentId){
        Course c = entityGetter.getCourse(courseName);
        //Professor p = entityGetter.getProfessor(professorId);
        Assignment assignment = entityGetter.getAssignment(assignmentId);

        if(!assignment.getCourse().equals(c))
            throw new AssignmentServiceException("Assignment doesn't belong to course " + courseName);


        // todo: tornare solo un draft per studente
        List<Draft> lastDrafts = new LinkedList<>();
        List<Student> students = assignment.getDrafts().stream().map(Draft::getStudent).distinct().collect(Collectors.toList());
        for (Student student: students){
            // non può essere nullo perchè gli studenti li abbiamo aggiunti dalla lista dei draft
            lastDrafts.add(assignment.getDrafts().stream().min((d1, d2) -> d2.getTimestamp().compareTo(d1.getTimestamp())).get());
        }
        return lastDrafts.stream()
                .map(a -> mapper.map(a, DraftDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void setDraftStatus(Long draftId, Draft.DraftState state) {
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
        //todo: rivedere
        draftRepository.findAll()
                .stream()
                .filter(d -> d.getAssignment()
                        .getExpiryDate()
                        .before(new Timestamp(System.currentTimeMillis())))
                .forEach(d -> d.setLocker(true));
    }

    @Override
    public void deleteAssignmentAndDraftsByCourseName(String courseName) {
        draftRepository.deleteByAssignmentCourseNameIgnoreCase(courseName);
        assignmentRepository.deleteByCourseNameIgnoreCase(courseName);
    }

    @PreAuthorize("@securityApiAuth.isMe(#professorId) && @securityApiAuth.ownCourse(#courseName)")
    @Override
    public StudentDTO getStudentForDraft(String professorId, String courseName, Long assignmentId, Long draftId) {
//        Professor professor = entityGetter.getProfessor(professorId);
//        Course course = entityGetter.getCourse(courseName);
        Assignment assignment = entityGetter.getAssignment(assignmentId);
        Draft draft = entityGetter.getDraft(draftId);
        if (draft.getAssignment().equals(assignment))
            return mapper.map(draft.getStudent(), StudentDTO.class);
        else throw new AssignmentNotOfThisDraftException(assignmentId, draftId);
    }

    @PreAuthorize("@securityApiAuth.isMe(#studentId) && @securityApiAuth.isEnrolled(#courseName) || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public List<DraftDTO> getDraftsForStudent(String studentId, String courseName, Long assignmentId) {
        Student s = entityGetter.getStudent(studentId);
        Assignment a = entityGetter.getAssignment(assignmentId);

        return draftRepository.findByAssignmentAndStudent(a,s).stream().map(d-> mapper.map(d, DraftDTO.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("@securityApiAuth.ownCourse(#courseName)")
    @Override
    public CorrectionDTO correctDraft(String professorId, String courseName, Long assignmentId, Long draftId, int grade) {
        Professor p = entityGetter.getProfessor(professorId);
        Course c = entityGetter.getCourse(courseName);
        Assignment a = entityGetter.getAssignment(assignmentId);
        Draft d = entityGetter.getDraft(draftId);

        //todo: fare controlli su assignment appartenente al corso e draft appartenente all'assignment

        //Non può lanciare eccezioni perchè viene controllato prima che il draft appartenga all'assignment
        Draft lastDraft = draftRepository.findByAssignmentAndStudentOrderByTimestampDesc(a, d.getStudent()).get(0);
        if(!lastDraft.equals(d))
            throw new DraftNotLastSubmittedException(draftId, d.getStudent().getId());

        lastDraft.setLocker(true);

        Correction corr = new Correction();
        Draft newDraft= new Draft(Draft.DraftState.REVIEWED, a, d.getStudent());
        newDraft.setLocker(grade!=0);
        newDraft.setGrade(grade);
        newDraft.setPhotoName(lastDraft.getPhotoName());
        newDraft = draftRepository.save(newDraft);
        corr.setDraft(newDraft);

        return mapper.map(correctionRepository.save(corr), CorrectionDTO.class);
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

    @PreAuthorize("@securityApiAuth.isMe(#professorId) && @securityApiAuth.ownCourse(#courseName)")
    @Override
    public int evaluateDraft(String professorId, String courseName, Long assignmentId, Long draftId, int grade) {
        Draft draft = entityGetter.getDraft(draftId);
        Assignment assignment = entityGetter.getAssignment(assignmentId);
        if(!draft.getAssignment().equals(assignment))
            throw new AssignmentNotOfThisDraftException(assignmentId, draftId);
        draft.setGrade(grade);
        return grade;
    }

    @PreAuthorize("@securityApiAuth.isMe(#studentId)")
    @Override
    public DraftDTO submitDraft(String studentId, String courseName, Long assignmentId) {
        Student s = entityGetter.getStudent(studentId);
        Course c = entityGetter.getCourse(courseName);
        Assignment a = entityGetter.getAssignment(assignmentId);

        if(!c.getStudents().contains(s))
            throw new StudentNotEnrolledException(studentId, courseName);

        if(!a.getCourse().equals(c))
            throw new AssignmentServiceException("Assignment doesn't belong to course " + courseName);

        List<Draft> drafts = draftRepository.findByAssignmentAndStudentOrderByTimestampDesc(a, s);

        if(drafts.isEmpty())
            throw new AssignmentServiceException(String.format("Student %s doesn't read the assignment yet", studentId));

        Draft lastDraft = drafts.get(0);
        if(lastDraft.isLocker())
            throw new AssignmentServiceException("Last draft has already been locked");

        lastDraft.setLocker(true);

        Draft newDraft = new Draft(Draft.DraftState.SUBMITTED, a, s);

        return mapper.map(draftRepository.save(newDraft), DraftDTO.class);
    }
}
