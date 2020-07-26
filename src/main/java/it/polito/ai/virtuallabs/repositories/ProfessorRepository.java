package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, String> {
    @Query("SELECT c.name FROM Professor p INNER JOIN p.courses c WHERE LOWER(p.id)=LOWER(:professorId)")
    List<String> getCourseNames(String professorId);
    Optional<Professor> findByIdIgnoreCase(String professorId);
}
