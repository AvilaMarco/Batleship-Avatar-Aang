package dev.racnar.shipbender.dto.request.websocket;

import dev.racnar.shipbender.enums.ShipType;
import lombok.Data;

import java.util.List;

@Data
public class ShipDTO {

    private List<String> locations;
    private List<String> salvosImpacted;
    private ShipType     type;
}
