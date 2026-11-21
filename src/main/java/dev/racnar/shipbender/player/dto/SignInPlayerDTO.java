package dev.racnar.shipbender.player.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Data
public class SignInPlayerDTO extends LoginPlayerDTO {

    @NotEmpty
    String name;
}
