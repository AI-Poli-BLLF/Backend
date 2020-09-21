package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    void deleteByCourseNameIgnoreCase(String courseName);
}
