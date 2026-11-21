package dev.racnar.shipbender.player.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotEmpty String name, @Email @Size(max = 30) @NotEmpty String email, @NotEmpty String password) {}
