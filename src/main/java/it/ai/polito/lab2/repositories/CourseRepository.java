package it.ai.polito.lab2.repositories;

import it.ai.polito.lab2.entities.Course;
import it.ai.polito.lab2.entities.Student;
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
            "(SELECT s FROM Student s INNER JOIN s.teams t INNER JOIN t.course c WHERE LOWER(c.name)=LOWER(:courseName))")
    List<Student> getStudentsNotInTeams(String courseName);

    Optional<Course> findByNameIgnoreCase(String courseName);
}
