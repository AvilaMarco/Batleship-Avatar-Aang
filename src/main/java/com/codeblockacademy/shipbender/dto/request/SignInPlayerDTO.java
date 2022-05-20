package com.codeblockacademy.shipbender.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SignInPlayerDTO extends LoginPlayerDTO {

    @NotEmpty
    String name;
}
