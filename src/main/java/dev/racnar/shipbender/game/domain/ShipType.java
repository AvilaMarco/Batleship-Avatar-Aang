package dev.racnar.shipbender.game.domain;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ShipType {
    CARRIER,
    BATTLESHIP,
    SUBMARINE,
    DESTROYER,
    PATROL_BOAT,
    @JsonEnumDefaultValue NONAME
}
