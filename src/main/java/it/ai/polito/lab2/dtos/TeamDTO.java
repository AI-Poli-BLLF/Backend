package it.ai.polito.lab2.dtos;

import it.ai.polito.lab2.entities.Team;
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
