package dev.racnar.shipbender.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Data
public class SignInPlayerDTO extends LoginPlayerDTO {

    @NotEmpty
    String name;
}
