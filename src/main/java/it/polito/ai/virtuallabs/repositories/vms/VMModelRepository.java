package it.polito.ai.virtuallabs.repositories.vms;

import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VMModelRepository extends JpaRepository<VMModel, String> {
    Optional<VMModel> findByIdIgnoreCase(String courseName);
    void deleteByIdIgnoreCase(String courseName);
}
