package com.codeblockacademy.shipbender.dto.request.websocket;

import com.codeblockacademy.shipbender.enums.ShipType;
import lombok.Data;

import java.util.List;

@Data
public class ShipDTO {

    private List<String> locations;
    private ShipType     type;
}
