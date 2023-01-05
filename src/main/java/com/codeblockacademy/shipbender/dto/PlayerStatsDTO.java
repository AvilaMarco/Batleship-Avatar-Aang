package com.codeblockacademy.shipbender.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerStatsDTO extends PlayerDTO {

    ScoreStatsDTO stats;

    public PlayerStatsDTO ( PlayerDTO playerDTO, ScoreStatsDTO stats ) {
        super(playerDTO.getId(), playerDTO.getName(), playerDTO.getEmail(), playerDTO.getNation());
        this.stats = stats;
    }

    public PlayerStatsDTO ( String name ) {
        super(name);
    }
}
