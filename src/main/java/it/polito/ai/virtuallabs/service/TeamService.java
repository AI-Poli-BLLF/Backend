package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.entities.Team;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

public interface TeamService {
    CourseDTO addCourse(CourseDTO course, String professorId);
    CourseDTO updateCourse(String oldCourseName, CourseDTO newCourse);
    Optional<CourseDTO> getCourse(String name);
    void deleteCourse(String name);
    List<CourseDTO> getAllCourses();
    List<CourseDTO> getAllCoursesByProfessor(String professorId);
    boolean addStudent(StudentDTO student);
    Optional<StudentDTO> getStudent(String studentId);
    List<StudentDTO> getAllStudents();
    List<StudentDTO> getEnrolledStudents(String courseName);
    boolean addStudentToCourse(String studentId, String courseName);
    void deleteStudentFromCourse(String courseName, String studentId);
    void enableCourse(String courseName);
    void disableCourse(String courseName);
    List<Boolean> addAll(List<StudentDTO> students);
    List<Boolean> enrollAll(List<String> studentIds, String courseName);
    List<Boolean> addAndEnroll(Reader r, String courseName);
    List<CourseDTO> getCourses(String studentId);
    List<TeamDTO> getTeamsForStudent(String studentId);
    List<StudentDTO> getMembers(String courseName, Long TeamId);
    StudentDTO getProposer(String courseName, Long teamId);
    TeamDTO proposeTeam(String courseName, String teamName, List<String> memberIds, String proposerId);
    List<TeamDTO> getTeamsForCourse(String courseName);
    List<TeamDTO> getActiveTeamsForCourse(String courseName);
    List<StudentDTO> getStudentsInTeams(String courseName);
    List<StudentDTO> getAvailableStudents(String courseName);
    Optional<TeamDTO> getTeam(String courseName, Long id);
    void setTeamStatus(Long teamId, Team.Status status);
    void evictTeam(Long teamId);
    void deleteTeam(String courseName, Long teamId);
    List<ProfessorDTO> getProfessors();
    Optional<ProfessorDTO> getProfessor(String professorId);
    boolean addProfessor(ProfessorDTO professorDTO);
    List<Long> evictPendingTeamsOfMembers(Long teamId);
    List<ProfessorDTO> getProfessorsOfCourse(String courseName);
}
