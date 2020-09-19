package it.polito.ai.virtuallabs.dtos.tokens;

import it.polito.ai.virtuallabs.entities.tokens.NotificationToken;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NotificationTokenDTO {
    @EqualsAndHashCode.Include
    @NotEmpty
    private String id;
    @NotEmpty
    private String senderId; //Id of the User who send the request
    @NotEmpty
    private String receiverId; //Id of the User who receive the request
    @NotEmpty
    private String courseName;
    @NotEmpty
    private String message;
    @NotNull
    private NotificationToken.NotificationType type;
    private boolean notificationRead;
}
