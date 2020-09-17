package it.polito.ai.virtuallabs.entities.tokens;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegistrationToken{
    @EqualsAndHashCode.Include
    @Id
    private String id;
    private String userId;
    private String userFirstName;
    private String userLastName;
    private Timestamp expiryDate;

    public RegistrationToken(String userId, String userFirstName, String userLastName) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.expiryDate = Timestamp.valueOf(LocalDateTime.now().plusHours(5));
    }
}
