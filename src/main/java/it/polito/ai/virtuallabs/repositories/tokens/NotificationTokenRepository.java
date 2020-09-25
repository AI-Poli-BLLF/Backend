package it.polito.ai.virtuallabs.repositories.tokens;

import it.polito.ai.virtuallabs.entities.tokens.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface NotificationTokenRepository extends JpaRepository<NotificationToken, String> {
    void deleteBySenderIdAndCourseName(String senderId, String courseName);
    List<NotificationToken> findByReceiverIdOrderByCreationDesc(String userId);
    long countBySenderIdAndReceiverIdAndCourseName(String senderId, String receiverId, String courseName);
    void deleteByCreationBefore(Timestamp timestamp);
}
