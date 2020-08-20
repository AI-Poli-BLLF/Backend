package it.polito.ai.virtuallabs.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequest {
    @Pattern(regexp = ".+@(studenti\\.)?polito\\.it",
            message = "User email must be in the form <userId>@(studenti.)polito.it")
    private String username;
    private String password;
}
