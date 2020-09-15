package it.polito.ai.virtuallabs.repositories.vms;

import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VMInstanceRepository extends JpaRepository<VMInstance, Long> {
    List<VMInstance> findAllByActiveTrue();
    List<VMInstance> findAllByActiveFalse();
    List<VMInstance> findAllByTeamId(Long teamId);
    List<VMInstance> findAllByTeamIdAndActiveTrue(Long teamId);
    List<VMInstance> findAllByTeamIdAndActiveFalse(Long teamId);

    void deleteByTeamCourseNameIgnoreCase(String courseName);
}
