package com.codeoftheweb.salvo.dto.request.websocket;

import com.codeoftheweb.salvo.enums.ShipType;
import lombok.Data;

import java.util.List;

@Data
public class ShipDTO {

    private List<String> locations;
    private ShipType     type;
}
