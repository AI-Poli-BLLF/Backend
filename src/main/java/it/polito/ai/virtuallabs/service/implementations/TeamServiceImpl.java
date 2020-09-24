package it.polito.ai.virtuallabs.service.implementations;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.entities.*;
import it.polito.ai.virtuallabs.repositories.*;
import it.polito.ai.virtuallabs.repositories.tokens.TokenRepository;
import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.VMService;
import it.polito.ai.virtuallabs.service.exceptions.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private VMService vmService;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityGetter entityGetter;

    @PreAuthorize("hasRole('PROFESSOR') && @securityApiAuth.isMe(#professorId)")
    @Override
    public CourseDTO addCourse(CourseDTO courseDTO, String professorId) {
        if(courseDTO.getMin() > courseDTO.getMax())
            throw new InconsistentCourseLimits(courseDTO.getMin(), courseDTO.getMax());

        if(courseRepository.findByNameIgnoreCase(courseDTO.getName()).isPresent())
            throw new CourseAlreadyExistException(courseDTO.getName());

        Course course = courseRepository.save(mapper.map(courseDTO, Course.class));

        Professor professor = entityGetter.getProfessor(professorId);

        course.addProfessor(professor);
        return mapper.map(course, CourseDTO.class);
    }

    // todo: è giusto l'admin messo così?
    @PreAuthorize("@securityApiAuth.ownCourse(#oldCourseName) || hasRole('ADMIN')")
    @Override
    public CourseDTO updateCourse(String oldCourseName, CourseDTO newCourse) {
        Course course = entityGetter.getCourse(oldCourseName);
        course.setName(newCourse.getName());
        course.setMin(newCourse.getMin());
        course.setMax(newCourse.getMax());
        course.setEnabled(newCourse.isEnabled());
        return mapper.map(course, CourseDTO.class);
    }

    //Permit All authenticated
    @Override
    @PreAuthorize("hasRole('PROFESSOR') || @securityApiAuth.isEnrolled(#courseName) || hasRole('ADMIN')")
    public Optional<CourseDTO> getCourse(String courseName) {
        Optional<Course> course = courseRepository.findByNameIgnoreCase(courseName);
        return course.map(c -> mapper.map(c, CourseDTO.class));
    }

    // todo: è giusto l'admin messo così?
    @PreAuthorize("@securityApiAuth.ownCourse(#courseName) || hasRole('ADMIN')")
    @Override
    public void deleteCourse(String courseName) {
        Course c = entityGetter.getCourse(courseName);
        assignmentService.deleteAssignmentAndDraftsByCourseName(courseName);
        vmService.deleteVmsByCourseName(courseName);
        teamRepository.deleteByCourse(c);
        courseRepository.delete(c);
    }

    //Permit to Every authenticated user
    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream().map(c-> mapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }

    //Permit to Every authenticated user
    @Override
    public List<CourseDTO> getAllCoursesByProfessor(String professorId) {
        Professor professor = entityGetter.getProfessor(professorId);
        return courseRepository.findByProfessors(professor).stream().map(c-> mapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }

    // todo: ma il peauth?
    //@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    @Override
    public boolean addStudent(StudentDTO student) {
        if(studentRepository.findByIdIgnoreCase(student.getId()).isPresent())
            return false;
        /*if(!managementService.createStudentUser(student))
            return false;*/
        studentRepository.save(mapper.map(student, Student.class));
        return true;
    }

    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN') || @securityApiAuth.isMe(#studentId)")
    @Override
    public Optional<StudentDTO> getStudent(String studentId) {
        Optional<Student> student = studentRepository.findByIdIgnoreCase(studentId);
        return student.map(s -> mapper.map(s, StudentDTO.class));
    }

    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream().map(s-> mapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.ownCourse(#courseName) || @securityApiAuth.isEnrolled(#courseName)")
    @Override
    public List<StudentDTO> getEnrolledStudents(String courseName) {
        try {
            return courseRepository.findByNameIgnoreCase(courseName).get().getStudents().stream()
                    .map(s-> mapper.map(s, StudentDTO.class)).collect(Collectors.toList());
        }catch (NoSuchElementException e){
            throw new CourseNotFoundException(courseName);
        }
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public boolean addStudentToCourse(String studentId, String courseName) {
        Student s;
        Course c;
        //Controllo esistenza studente e corso
        try{
            s = studentRepository.findByIdIgnoreCase(studentId).get();
        }catch (NoSuchElementException e){
            throw new StudentNotFoundException(studentId);
        }
        try{
            c = courseRepository.findByNameIgnoreCase(courseName).get();
        }catch (NoSuchElementException e){
            throw new CourseNotFoundException(courseName);
        }

        //Controllo che il corso sia abilitato
        if(!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        //Se la relazione esiste già ritorna false
        if(c.getStudents().contains(s) && s.getCourses().contains(c))
            return false;

        //Altrimenti aggiunge e ritorna true
        c.addStudent(s);
        return true;
    }

    //todo: gestire cosa succede ai team e alle VM quando lo studente viene rimosso dal corso
    @PreAuthorize("@securityApiAuth.ownCourse(#courseName) || hasRole('ADMIN')")
    @Override
    public void deleteStudentFromCourse(String courseName, String studentId) {
        Course c = entityGetter.getCourse(courseName);
        Student student = entityGetter.getStudent(studentId);

        if(!c.getStudents().contains(student))
            throw new StudentNotEnrolledException(studentId, courseName);

        Team t = student.getTeams().stream().filter(team-> team.getCourse().equals(c)).findFirst()
                .orElse(null);

        if(t != null) {
            vmService.deleteVmsByTeamId(t.getId());
            teamRepository.delete(t);
            t.removeMember(student);
        }

        student.removeCourse(c);
        System.out.println("DELETE "+ student.getFirstName()+" STUDENT FROM COURSE");
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public void enableCourse(String courseName) {
        try{
            Course c = courseRepository.findByNameIgnoreCase(courseName).get();
            c.setEnabled(true);
        }catch (NoSuchElementException e){
            throw new CourseNotFoundException(courseName);
        }
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public void disableCourse(String courseName) {
        try{
            Course c = courseRepository.findByNameIgnoreCase(courseName).get();
            c.setEnabled(false);
        }catch (NoSuchElementException e){
            throw new CourseNotFoundException(courseName);
        }
    }

    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    @Override
    public List<Boolean> addAll(List<StudentDTO> students) {
        List<Boolean> res = new ArrayList<>();
        students.forEach(s-> res.add(addStudent(s)));
        return res;
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public List<Boolean> enrollAll(List<String> studentIds, String courseName) {
        try {
            List<Boolean> res = new ArrayList<>();
            studentIds.forEach(s -> res.add(addStudentToCourse(s, courseName)));
            return res;
        }catch (TeamServiceException e){
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public void enrollByCsv(Reader r, String courseName) {
        Course c = entityGetter.getCourse(courseName);

        if(!c.isEnabled())
            throw new CourseNotEnabledException(courseName);

        CsvToBean<StudentDTO> csvToBean = new CsvToBeanBuilder(r)
                .withType(StudentDTO.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        Set<StudentDTO> studentsToEnroll = csvToBean.parse().stream()
                .map(StudentDTO::toLowerCase).collect(Collectors.toSet());
        Set<Student> studentsEntities = studentsToEnroll.stream()
                .map(s-> entityGetter.getStudent(s.getId())).collect(Collectors.toSet());

        Set<StudentDTO> studentCheck = studentsEntities.stream().map(s -> mapper.map(s, StudentDTO.class).toLowerCase())
                .collect(Collectors.toSet());

        if(!studentsToEnroll.equals(studentCheck))
            throw new InconsistentStudentDataException();

        List<Student> enrolled = c.getStudents();
        studentsEntities.forEach(s-> {
            if(!enrolled.contains(s))
                c.addStudent(s);
        });
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.isMe(#studentId)")
    @Override
    public List<CourseDTO> getCourses(String studentId) {
        try{
            Student s = studentRepository.findByIdIgnoreCase(studentId).get();
            return s.getCourses().stream()
                    .map(c-> mapper.map(c, CourseDTO.class))
                    .collect(Collectors.toList());
        }catch (NoSuchElementException e){
            throw new StudentNotFoundException(studentId);
        }
    }

    @Override
    public List<CourseDTO> getAvailableCoursesForStudent(String studentId) {
        Student s = entityGetter.getStudent(studentId);
        return courseRepository.getAvailableCourseForStudent(s).stream()
                .map(c-> mapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.isMe(#studentId)")
    @Override
    public List<TeamDTO> getTeamsForStudent(String studentId) {
        try {
            return studentRepository.findByIdIgnoreCase(studentId).get().getTeams()
                    .stream().map(t -> mapper.map(t, TeamDTO.class))
                    .collect(Collectors.toList());
        }catch (NoSuchElementException e){
            throw new StudentNotFoundException(studentId);
        }
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.doesTeamBelongsToOwnedOrEnrolledCourses(#teamId)")
    @Override
    public List<StudentDTO> getMembers(String courseName, Long teamId) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );
        Team t = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );

        if (!t.getCourse().getName().equals(c.getName()))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        return t.getMembers().stream()
                .map(m->mapper.map(m, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO getProposer(String courseName, Long teamId) {
        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );
        Team t = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );

        if (!t.getCourse().getName().equals(c.getName()))
            throw new TeamNotBelongToCourseException(t.getName(), courseName);

        return mapper.map(t.getProposer(), StudentDTO.class);
    }


    @PreAuthorize("@securityApiAuth.isMe(#proposerId)")
    @Override
    public TeamDTO proposeTeam(String courseName, String teamName, List<String> memberIds, String proposerId) {
        //Controllo che non ci siano duplicati all'interno dei memberIds
        int diff = memberIds.size() - new HashSet<>(
                memberIds.stream().map(String::toLowerCase).collect(Collectors.toList())
        ).size();
        if(diff != 0)
            throw new DuplicateStudentException(diff);
        //Controllo che il corso esista
        Optional<Course> oc = courseRepository.findByNameIgnoreCase(courseName);
        if(!oc.isPresent())
            throw new CourseNotFoundException(courseName);

        //Controllo che il corso sia abilitato
        Course c = oc.get();
        if(!c.isEnabled())
            throw new CourseNotEnabledException(c.getName());

        List<Student> enrolledStudents = c.getStudents();
        List<Student> members = new ArrayList<>();  //Conterrà la lista dei membri del gruppo

        memberIds.forEach(m -> {
            try {
                Student s = studentRepository.findByIdIgnoreCase(m).get(); //Throw NoSuchElementException se lo student non esiste nel db
                if(!enrolledStudents.contains(s))
                    throw new StudentNotEnrolledException(m, courseName);

                //Controlla che ogni studente non appartenga già ad un gruppo per il corso selezionato
                if(s.getTeams().stream().filter(t -> t.getStatus()== Team.Status.ACTIVE).map(Team::getCourse)
                        .collect(Collectors.toList()).contains(c))
                    throw new StudentAlreadyBelongsToTeam(s.getId(), courseName);

                members.add(s);
            } catch (NoSuchElementException e) {
                throw new StudentNotFoundException(m);
            }
        });


        //Controllo che la dimensione del gruppo sia entro i limiti dichiarati nel corso
        int teamSize = memberIds.size();
        if(teamSize < c.getMin() || teamSize > c.getMax())
            throw new TeamSizeOutOfBoundException(c.getMin(), c.getMax());

        //Controllo che il proponente sia elencato fra i membri
        if (!memberIds.contains(proposerId))
            throw new TeamProposerIsNotMemberException(proposerId);

        Student proposer = entityGetter.getStudent(proposerId);

        try {
            //Se tutto è andato bene creo il team
            Team team = new Team();
            team.setName(teamName);
            team.setStatus(Team.Status.PENDING);
            team.setCourse(c);
            team.setMembers(members);
            team.setProposer(proposer);

            return mapper.map(teamRepository.saveAndFlush(team), TeamDTO.class);
        }catch (DataIntegrityViolationException e){
            throw new TeamAlreadyExistException(teamName, courseName);
        }
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.isEnrolled(#courseName) || @securityApiAuth.ownCourse(#courseName)")
    @Override
    public List<TeamDTO> getTeamsForCourse(String courseName) {
        try {
            return courseRepository.findByNameIgnoreCase(courseName).get()
                    .getTeams().stream()
                    .map(t-> mapper.map(t, TeamDTO.class))
                    .collect(Collectors.toList());
        }catch (NoSuchElementException e){
            throw new CourseNotFoundException(courseName);
        }

    }

    @PreAuthorize("@securityApiAuth.ownCourse(#courseName) ||@securityApiAuth.isEnrolled(#courseName)")
    @Override
    public List<StudentDTO> getStudentsInTeams(String courseName) {
        if(!courseRepository.findByNameIgnoreCase(courseName).isPresent())
            throw new CourseNotFoundException(courseName);

        return courseRepository.getStudentsInTeams(courseName)
                .stream().map(s->mapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("@securityApiAuth.ownCourse(#courseName) ||@securityApiAuth.isEnrolled(#courseName)")
    @Override
    public List<StudentDTO> getAvailableStudents(String courseName) {

        if(!courseRepository.findByNameIgnoreCase(courseName).isPresent())
            throw new CourseNotFoundException(courseName);

        return courseRepository.getStudentsNotInTeams(courseName)
                .stream().map(s->mapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void setTeamStatus(Long teamId, Team.Status status) {
        try {
            Team team = teamRepository.findById(teamId).get();
            team.setStatus(status);
        }catch (NoSuchElementException e){
            throw new TeamNotFoundException(teamId);
        }
    }

    @PreAuthorize("hasRole('ADMIN') || @securityApiAuth.doesTeamBelongsToOwnedOrEnrolledCourses(#id)")
    @Override
    public Optional<TeamDTO> getTeam(String courseName, Long id) {
        /*if (!courseRepository.findByNameIgnoreCase(courseName).isPresent())
            throw new CourseNotFoundException(courseName);*/

        Course c = courseRepository.findByNameIgnoreCase(courseName).orElseThrow(
                () -> new CourseNotFoundException(courseName)
        );

        Optional<Team> team = teamRepository.findById(id);
        if (team.isPresent() && !team.get().getCourse().equals(c))
            throw new TeamNotBelongToCourseException(team.get().getName(), courseName);

        return team.map(t -> mapper.map(t, TeamDTO.class));
    }

    @Override
    public void evictTeam(Long teamId) {
        teamRepository.deleteById(teamId);
    }

    //Permit all authenticated
    @Override
    public Optional<ProfessorDTO> getProfessor(String professorId) {
        Optional<Professor> professor = professorRepository.findByIdIgnoreCase(professorId);
        return professor.map(p -> mapper.map(p, ProfessorDTO.class));
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @Override
    public boolean addProfessor(ProfessorDTO professorDTO) {
        if(professorRepository.findByIdIgnoreCase(professorDTO.getId()).isPresent())
            return false;
        /*if(!managementService.createProfessorUser(professorDTO))
            return false;*/
        professorRepository.save(mapper.map(professorDTO, Professor.class));
        return true;
    }

    @Override
    public List<Long> evictPendingTeamsOfMembers(Long teamId) {
        // 0. get team
        Optional<Team> team = teamRepository.findById(teamId);
        if (!team.isPresent()) {
            throw new TeamNotFoundException();
        }

        // 1. get team members
        List<Student> members = team.get().getMembers();

        // 2. get members' teams referring to te same course
        List<Long> teamsToEvict = new ArrayList<>();
        for (Student s : members) {
            List<Long> studentTeamsToEvict = s.getTeams().stream()
                    .filter(t -> !t.getId().equals(team.get().getId()))
                    .filter(t -> t.getCourse() == team.get().getCourse())
                    .map(Team::getId)
                    .collect(Collectors.toList());

            for (Long teamIdToEvict : studentTeamsToEvict) {
                if (!teamsToEvict.contains(teamIdToEvict)) {
                    teamsToEvict.add(teamIdToEvict);
                }
            }
        }

        // 3. delete pending teams
        for (Long id : teamsToEvict) {
            evictTeam(id);
        }

        // 4. delete tokens (done by the caller)
        return teamsToEvict;
    }

    // todo: controllare che sia giusto
    @Override
    public void deleteTeam(String courseName, Long teamId) {
        Team team = entityGetter.getTeam(teamId);
        if (!team.getCourse().getName().equals(courseName))
            throw new TeamNotBelongToCourseException();
        vmService.deleteVmsByTeamId(team.getId());
        teamRepository.delete(team);
        System.out.println("Team: " + teamRepository.findById(teamId).isPresent());
    }

    //Permit all authenticated
    @Override
    public List<ProfessorDTO> getProfessors() {
        List<ProfessorDTO> professors = professorRepository.findAll().stream()
                .map(p->mapper.map(p, ProfessorDTO.class))
                .collect(Collectors.toList());
        professors.forEach(p-> p.setCourseNames(professorRepository.getCourseNames(p.getId())));
        return professors;
    }

    @Override
    public List<ProfessorDTO> getProfessorsOfCourse(String courseName) {
        List<ProfessorDTO> professorDTOS = entityGetter.getCourse(courseName).getProfessors()
                .stream().map(p-> mapper.map(p, ProfessorDTO.class))
                .collect(Collectors.toList());
        professorDTOS.forEach(p-> p.setCourseNames(professorRepository.getCourseNames(p.getId())));
        return professorDTOS;
    }
}
