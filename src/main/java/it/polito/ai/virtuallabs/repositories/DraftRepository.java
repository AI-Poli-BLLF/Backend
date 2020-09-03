package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Draft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftRepository extends JpaRepository<Draft, String> {
}
