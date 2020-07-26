package it.polito.ai.virtuallabs.repositories.vms;

import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VMInstanceRepository extends JpaRepository<VMInstance, Long> {
}
