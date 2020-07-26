package it.polito.ai.virtuallabs.dtos;

import it.polito.ai.virtuallabs.entities.Team;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TeamDTO extends RepresentationModel<TeamDTO> {
    @NotNull
    private Long id;
    @NotEmpty
    private String name;
    @NotNull
    private Team.Status status;
}
