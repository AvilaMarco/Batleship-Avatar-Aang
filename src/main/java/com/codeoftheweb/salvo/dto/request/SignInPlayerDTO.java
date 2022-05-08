package com.codeoftheweb.salvo.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class SignInPlayerDTO {

    @NotEmpty
    String name;

    @Email
    @Size(min = 0, max = 30)
    @NotEmpty
    String email;

    @NotEmpty
    String password;
}
