package it.polito.ai.virtuallabs.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO extends RepresentationModel<AssignmentDTO> {

    @EqualsAndHashCode.Include
    @Id
    private String id;

    @EqualsAndHashCode.Include
    private Timestamp releaseDate;

    @EqualsAndHashCode.Include
    private Timestamp expiryDate;

}
