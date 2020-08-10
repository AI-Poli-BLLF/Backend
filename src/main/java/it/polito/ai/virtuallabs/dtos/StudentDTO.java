package it.polito.ai.virtuallabs.dtos;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO extends RepresentationModel<StudentDTO> {

    @EqualsAndHashCode.Include
    @CsvBindByName
    @Pattern(regexp = "s[0-9]{6}", message = "The id must be in the following format s<id>")
    private String id;

    @EqualsAndHashCode.Include
    @CsvBindByName
    @NotEmpty
    private String name;

    @EqualsAndHashCode.Include
    @CsvBindByName
    @NotEmpty
    private String firstName;

    private String photoName;

    public String getEmail(){
        return String.format("%s@studenti.polito.it", id);
    }
}
