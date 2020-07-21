package it.ai.polito.lab2.dtos;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO extends RepresentationModel<StudentDTO> {

    @CsvBindByName
    @Pattern(regexp = "s[0-9]+", message = "The id must be in the following format s<id>")
    private String id;

    @CsvBindByName
    @NotEmpty
    private String name;

    @CsvBindByName
    @NotEmpty
    private String firstName;

    public String getEmail(){
        return String.format("%s@studenti.polito.it", id);
    }
}
