package dev.racnar.shipbender.player.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record LoginRequest(@Email @Size(max = 30) @NotEmpty String email, @Size(max = 72) @NotEmpty String password) {}
