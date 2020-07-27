package it.polito.ai.virtuallabs.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorDTO extends RepresentationModel<ProfessorDTO> {
    @EqualsAndHashCode.Include
    @Pattern(regexp = "d[0-9]+", message = "The id must be in the following format d<id>")
    private String id;

    @EqualsAndHashCode.Include
    @NotEmpty
    private String name;

    @EqualsAndHashCode.Include
    @NotEmpty
    private String firstName;
    private String photoName;
    private List<String> courseNames = new ArrayList<>();

    public String getEmail(){
        return String.format("%s@polito.it", id);
    }
}
