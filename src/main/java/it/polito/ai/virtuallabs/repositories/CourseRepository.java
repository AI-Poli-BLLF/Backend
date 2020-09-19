package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.Professor;
import it.polito.ai.virtuallabs.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    @Query("SELECT s FROM Student s INNER JOIN s.teams t INNER JOIN t.course c WHERE LOWER(c.name)=LOWER(:courseName)")
    List<Student> getStudentsInTeams(String courseName);

    @Query("SELECT s FROM Course c INNER JOIN c.students s WHERE c.name=:courseName AND s NOT IN " +
            "(SELECT s FROM Student s INNER JOIN s.teams t INNER JOIN t.course c WHERE LOWER(c.name)=LOWER(:courseName)" +
            " AND t.status=1)")
    List<Student> getStudentsNotInTeams(String courseName);

    Optional<Course> findByNameIgnoreCase(String courseName);

    List<Course> findByProfessors(Professor professor);

    void deleteByNameIgnoreCase(String courseName);
}
