package it.polito.ai.virtuallabs.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DraftDTO extends RepresentationModel<DraftDTO> {
    @EqualsAndHashCode.Include
    private String id;

    @EqualsAndHashCode.Include
    private String state;

    @EqualsAndHashCode.Include
    private int grade;

    @EqualsAndHashCode.Include
    private Timestamp timestamp;

    private boolean lock;
}
