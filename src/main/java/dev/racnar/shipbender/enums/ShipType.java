package dev.racnar.shipbender.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ShipType {
    CARRIER,
    BATTLESHIP,
    SUBMARINE,
    DESTROYER,
    PATROL_BOAT,
    @JsonEnumDefaultValue NONAME
}
