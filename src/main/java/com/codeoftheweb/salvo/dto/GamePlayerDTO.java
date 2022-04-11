package com.codeoftheweb.salvo.dto;

import com.codeoftheweb.salvo.dto.response.PlayerDTO;
import com.codeoftheweb.salvo.enums.NationType;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
public class GamePlayerDTO {

    Long id;

    Integer playerId;
    Integer score;
}
