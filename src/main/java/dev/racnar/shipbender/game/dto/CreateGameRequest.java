package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.shared.NationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGameRequest(@NotNull NationType nation, @NotBlank @Size(max = 32) String location) {}
