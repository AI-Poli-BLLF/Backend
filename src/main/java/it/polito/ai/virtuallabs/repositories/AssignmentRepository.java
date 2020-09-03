package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, String> {

}
