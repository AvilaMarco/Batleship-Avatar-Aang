package dev.racnar.shipbender.player.dto;

import dev.racnar.shipbender.shared.NationType;
import jakarta.validation.constraints.NotNull;

public record UpdatePlayerRequest(@NotNull NationType nation) {}
