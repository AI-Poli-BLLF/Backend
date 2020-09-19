package it.polito.ai.virtuallabs.entities.tokens;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NotificationToken {
    public enum NotificationType{STUDENT_ENROLLING, PROFESSOR_COOPERATION, RESPONSE}
    @EqualsAndHashCode.Include
    @Id
    private String id;
    private String senderId; //Id of the User who send the request
    private String receiverId; //Id of the User who receive the request
    private String courseName;
    private String message;
    private Timestamp expiryDate;
    private NotificationType type;
    private boolean notificationRead; //Indicates if the notification has been read or not (reason of visualization)

    public NotificationToken() {
        this.id = UUID.randomUUID().toString();
        this.expiryDate = Timestamp.valueOf(LocalDateTime.now().plusDays(5));
        this.notificationRead = false;
    }

    public NotificationToken(String senderId, String receiverId, String courseName, String message, NotificationType type) {
        this();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.courseName = courseName;
        this.message = message;
        this.type = type;
    }
}
