package it.polito.ai.virtuallabs.dtos;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftDTO extends RepresentationModel<DraftDTO> {

    public enum State{NULL, READ, SUBMITTED, REVIEWED}

    @Id
    private Long id;

    @EqualsAndHashCode.Include
    private State state;

    @EqualsAndHashCode.Include
    private int grade;

    @EqualsAndHashCode.Include
    private Timestamp timestamp;

    private boolean locker;
}
