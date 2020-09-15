package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Assignment;
import it.polito.ai.virtuallabs.entities.Draft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DraftRepository extends JpaRepository<Draft, String> {
    void deleteByAssignmentCourseNameIgnoreCase(String courseName);
}
