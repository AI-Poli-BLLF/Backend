package it.polito.ai.virtuallabs.repositories;

import it.polito.ai.virtuallabs.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
