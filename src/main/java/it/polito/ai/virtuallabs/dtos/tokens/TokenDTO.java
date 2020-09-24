package it.polito.ai.virtuallabs.dtos.tokens;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TokenDTO {

    @NotEmpty
    private String id;
    @NotEmpty
    private Long teamId;
    @NotEmpty
    private String studentId;
    @NotEmpty
    private Timestamp expiryDate;

    public TokenDTO(String id, Long teamId, String studentId, Timestamp expiryDate) {
        this.id = id;
        this.teamId = teamId;
        this.studentId = studentId;
        this.expiryDate = expiryDate;
    }
}
