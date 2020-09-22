package it.polito.ai.virtuallabs.dtos;

import it.polito.ai.virtuallabs.entities.Draft;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftDTO extends RepresentationModel<DraftDTO> {

    @Id
    private Long id;

    private String photoName;

    @EqualsAndHashCode.Include
    private Draft.DraftState state;

    @EqualsAndHashCode.Include
    private int grade;

    @EqualsAndHashCode.Include
    private Timestamp timestamp;

    private boolean locker;
}
