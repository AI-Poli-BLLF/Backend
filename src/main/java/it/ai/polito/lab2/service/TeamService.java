package it.ai.polito.lab2.service;

import it.ai.polito.lab2.dtos.CourseDTO;
import it.ai.polito.lab2.dtos.ProfessorDTO;
import it.ai.polito.lab2.dtos.StudentDTO;
import it.ai.polito.lab2.dtos.TeamDTO;
import it.ai.polito.lab2.entities.Team;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

public interface TeamService {
    boolean addCourse(CourseDTO course);
    Optional<CourseDTO> getCourse(String name);
    List<CourseDTO> getAllCourses();
    boolean addStudent(StudentDTO student);
    Optional<StudentDTO> getStudent(String studentId);
    List<StudentDTO> getAllStudents();
    List<StudentDTO> getEnrolledStudents(String courseName);
    boolean addStudentToCourse(String studentId, String courseName);
    void enableCourse(String courseName);
    void disableCourse(String courseName);
    List<Boolean> addAll(List<StudentDTO> students);
    List<Boolean> enrollAll(List<String> studentIds, String courseName);
    List<Boolean> addAndEnroll(Reader r, String courseName);
    List<CourseDTO> getCourses(String studentId);
    List<TeamDTO> getTeamsForStudent(String studentId);
    List<StudentDTO>getMembers(Long TeamId);
    TeamDTO proposeTeam(String courseName, String teamName, List<String> memberIds);
    List<TeamDTO> getTeamForCourse(String courseName);
    List<StudentDTO> getStudentsInTeams(String courseName);
    List<StudentDTO> getAvailableStudents(String courseName);
    Optional<TeamDTO> getTeam(Long id);
    void setTeamStatus(Long teamId, Team.Status status);
    void evictTeam(Long teamId);
    List<ProfessorDTO> getProfessors();
    Optional<ProfessorDTO> getProfessor(String professorId);
    public boolean addProfessor(ProfessorDTO professorDTO);
}
