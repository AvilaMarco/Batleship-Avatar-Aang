package dev.racnar.shipbender.player.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Data
public class LoginPlayerDTO {
    @Email
    @Size(max = 30)
    @NotEmpty
    String email;

    @NotEmpty
    String password;
}
