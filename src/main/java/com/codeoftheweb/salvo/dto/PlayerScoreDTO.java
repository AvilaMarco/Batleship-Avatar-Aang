package com.codeoftheweb.salvo.dto;

import com.codeoftheweb.salvo.enums.NationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerScoreDTO extends PlayerDTO {

    List<Integer> scores;

    public PlayerScoreDTO(PlayerDTO playerDTO, List<Integer> scores) {
        super(playerDTO.getId(), playerDTO.getName(), playerDTO.getEmail(), playerDTO.getNation());
        this.scores = scores;
    }
}
