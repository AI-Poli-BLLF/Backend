package it.ai.polito.lab2.dtos;

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
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorDTO extends RepresentationModel<ProfessorDTO> {
    @Pattern(regexp = "d[0-9]+", message = "The id must be in the following format d<id>")
    private String id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String firstName;
    private List<String> courseNames = new ArrayList<>();

    public String getEmail(){
        return String.format("%s@polito.it", id);
    }
}
