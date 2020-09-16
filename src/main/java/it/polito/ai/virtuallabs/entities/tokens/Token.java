package it.polito.ai.virtuallabs.entities.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Token {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private Long teamId;
    private String studentId;
    private Timestamp expiryDate;
}
