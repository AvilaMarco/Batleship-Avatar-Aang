package dev.racnar.shipbender.player.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

public record RegisterRequest(@Size(max = 50) @NotEmpty String name, @Email @Size(max = 30) @NotEmpty String email, @Size(min = 8, max = 72) @NotEmpty String password) {

    // bcrypt lanza IllegalArgumentException (500) con más de 72 BYTES; @Size cuenta caracteres.
    @JsonIgnore
    @AssertTrue
    public boolean isPasswordWithin72Bytes () {
        return password == null || password.getBytes( StandardCharsets.UTF_8 ).length <= 72;
    }
}
