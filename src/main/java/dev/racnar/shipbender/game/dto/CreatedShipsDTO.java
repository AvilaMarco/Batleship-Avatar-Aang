package dev.racnar.shipbender.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatedShipsDTO {
    StatusGameDTO status;
    List<ShipDTO> ships;
    Long          playerId;
}
