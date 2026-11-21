package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.game.domain.ShipType;
import lombok.Data;

import java.util.List;

@Data
public class ShipDTO {

    private List<String> locations;
    private List<String> salvosImpacted;
    private ShipType     type;
}
