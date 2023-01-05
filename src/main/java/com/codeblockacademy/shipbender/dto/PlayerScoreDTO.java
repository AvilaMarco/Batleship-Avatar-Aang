package com.codeblockacademy.shipbender.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PlayerScoreDTO extends PlayerDTO {
    Integer score;

    public PlayerScoreDTO ( PlayerDTO playerDTO, Integer score ) {
        super(playerDTO.getId(), playerDTO.getName(), playerDTO.getEmail(), playerDTO.getNation());
        this.score = score;
    }

    public PlayerScoreDTO ( String name ) {
        super(name);
    }
}
