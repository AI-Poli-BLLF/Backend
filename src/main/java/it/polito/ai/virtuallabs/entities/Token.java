package it.polito.ai.virtuallabs.entities;

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
    public enum TokenType{REGISTRATION, TEAM_PROPOSAL, COURSE_ENROLLING}
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private Long teamId = -1L;
    private String studentId;
    private Timestamp expiryDate;
    private TokenType type;

    //USED TO CREATE REGISTRATION TOKEN
    public Token(String userId, TokenType type){
        this.id = UUID.randomUUID().toString();
        this.studentId = userId;
        this.expiryDate = Timestamp.valueOf(LocalDateTime.now().plusHours(24));
        this.type = type;
    }
}
