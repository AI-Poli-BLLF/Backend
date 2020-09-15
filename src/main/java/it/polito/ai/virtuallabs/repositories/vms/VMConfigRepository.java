package it.polito.ai.virtuallabs.repositories.vms;

import it.polito.ai.virtuallabs.entities.vms.VMConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VMConfigRepository extends JpaRepository<VMConfig, Long> {
    void deleteByTeamCourseNameIgnoreCase(String courseName);
}
