package it.polito.ai.virtuallabs.repositories.tokens;

import it.polito.ai.virtuallabs.entities.tokens.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTokenRepository extends JpaRepository<NotificationToken, String> {
    void deleteBySenderIdAndCourseName(String senderId, String courseName);
}
