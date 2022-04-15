package com.codeoftheweb.salvo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerScoreDTO extends PlayerDTO {

    ScoreStatsDTO stats;

    public PlayerScoreDTO ( PlayerDTO playerDTO, ScoreStatsDTO stats ) {
        super(playerDTO.getId(), playerDTO.getName(), playerDTO.getEmail(), playerDTO.getNation());
        this.stats = stats;
    }

    public PlayerScoreDTO ( String name ) {
        super(name);
    }
}
