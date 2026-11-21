package dev.racnar.shipbender.game.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SalvoRequest(@NotNull List<String> cells) {}
