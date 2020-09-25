package it.polito.ai.virtuallabs.dtos.vms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VMOsDTO {
    private Long id;
    @NotEmpty
    private String osName;
    private Set<String> versions = new HashSet<>();
}
