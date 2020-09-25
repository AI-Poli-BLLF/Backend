package it.polito.ai.virtuallabs.security.dtos;

import lombok.Data;
import org.checkerframework.common.value.qual.MinLen;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistration {

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String name;
    @Pattern(regexp = "[sSdD][0-9]{6}", message = "User id must be in the form s<six numbers> or d<six numbers>")
    private String userId;
    @MinLen(8)
    private String password;
    @Pattern(regexp = "[sSdD][0-9]{6}@(studenti\\.)?polito\\.it",
    message = "User email must be in the form <userId>@(studenti.)polito.it")
    private String email;
}
