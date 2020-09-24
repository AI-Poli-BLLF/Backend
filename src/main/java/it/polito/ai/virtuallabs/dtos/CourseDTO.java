package it.polito.ai.virtuallabs.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Arrays;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CourseDTO extends RepresentationModel<CourseDTO> {

    @EqualsAndHashCode.Include
    @NotEmpty
    private String name;

    @EqualsAndHashCode.Include
    @Min(0)
    private int min;

    @EqualsAndHashCode.Include
    @Max(0)
    private int max;

    private boolean enabled;

    public CourseDTO(String name, int min, int max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public String getAcronym(){
        String[] pieces = name.split("\\s+");
        return Arrays.stream(pieces).map(p-> Character.toString(Character.toUpperCase(p.toCharArray()[0])))
        .reduce((p1,p2) -> p1+p2).orElse(this.name);
    }
}
