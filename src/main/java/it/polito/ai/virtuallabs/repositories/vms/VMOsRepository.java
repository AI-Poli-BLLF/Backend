package it.polito.ai.virtuallabs.repositories.vms;

import it.polito.ai.virtuallabs.entities.vms.VMOs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VMOsRepository extends JpaRepository<VMOs, String> {
    Optional<VMOs> findByOsNameIgnoreCase(String osName);
    void deleteByOsNameIgnoreCase(String osName);
}
