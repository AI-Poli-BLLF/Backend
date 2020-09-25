package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Correction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    void deleteAllByDraftAssignmentCourseNameIgnoreCase(String CourseName);
}
