package it.ai.polito.lab2.repositories;

import it.ai.polito.lab2.entities.VMModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VMModelRepository extends JpaRepository<VMModel, String> {
}
