package com.codeoftheweb.salvo.dto.response;

import com.codeoftheweb.salvo.models.Salvo;
import com.codeoftheweb.salvo.models.Ship;
import lombok.Data;
import org.springframework.data.convert.ThreeTenBackPortConverters;

import java.time.LocalDateTime;
import java.util.List;

// TODO: Delete ?

@Data
public class GameViewDTO {

    Long gameId;
    String location;
    String direction;
    String type;

    List<Ship> ships;
    List<Salvo> salvos;

    // TODO: Delete ?
    LocalDateTime created;
}
