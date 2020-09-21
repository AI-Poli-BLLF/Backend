package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Assignment;
import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.Draft;
import it.polito.ai.virtuallabs.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DraftRepository extends JpaRepository<Draft, Long> {
    List<Draft> findByAssignmentAndStudent(Assignment assignment, Student student);
    List<Draft> findByAssignmentCourseAndStudent(Course course, Student student);
    void deleteByAssignmentCourseNameIgnoreCase(String courseName);

}
