package it.polito.ai.virtuallabs.repositories.vms;

import it.polito.ai.virtuallabs.entities.vms.VMModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VMModelRepository extends JpaRepository<VMModel, String> {
}
