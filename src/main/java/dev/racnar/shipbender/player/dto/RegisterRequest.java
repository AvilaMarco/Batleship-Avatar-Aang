package dev.racnar.shipbender.player.dto;

import dev.racnar.shipbender.shared.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// bcrypt lanza IllegalArgumentException (500) con más de 72 BYTES; @Size cuenta caracteres.
public record RegisterRequest(@Size(max = 50) @NotEmpty String name, @Email @Size(max = 30) @NotEmpty String email,
                              @Size(min = 8, max = 72) @NotEmpty @MaxUtf8Bytes(value = 72, message = "no puede superar 72 bytes en UTF-8 (los caracteres no ASCII ocupan más de uno)") String password) {}
