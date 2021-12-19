package com.codeoftheweb.salvo.dto;

import com.codeoftheweb.salvo.enums.NationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerScoreDTO extends PlayerDTO {

    List<Integer> scores;

    public PlayerScoreDTO(Long id, String name, String email, NationType nation, List<Integer> scores) {
        super(id, name, email, nation);
        this.scores = scores;
    }
}
