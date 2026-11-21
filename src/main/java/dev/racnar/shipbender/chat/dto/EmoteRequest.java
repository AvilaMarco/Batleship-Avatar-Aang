package dev.racnar.shipbender.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmoteRequest(@NotBlank @Size(max = 32) String emote) {}
