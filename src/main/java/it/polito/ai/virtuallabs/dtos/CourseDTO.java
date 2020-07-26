package it.polito.ai.virtuallabs.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CourseDTO extends RepresentationModel<CourseDTO> {

    @EqualsAndHashCode.Include
    @NotEmpty
    private String name;

    @EqualsAndHashCode.Include
    private int min;

    @EqualsAndHashCode.Include
    private int max;

    private boolean enabled;

    public CourseDTO(String name, int min, int max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }
}
