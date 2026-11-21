package dev.racnar.shipbender.game.domain;

import java.util.List;

public record ShipPlacement(ShipType type, List<Cell> cells) {}
