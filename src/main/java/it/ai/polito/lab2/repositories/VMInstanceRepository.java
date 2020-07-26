package it.ai.polito.lab2.repositories;

import it.ai.polito.lab2.entities.VMInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VMInstanceRepository extends JpaRepository<VMInstance, Long> {
}
