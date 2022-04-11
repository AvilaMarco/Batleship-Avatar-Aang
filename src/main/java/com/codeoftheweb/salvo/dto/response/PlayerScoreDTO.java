package com.codeoftheweb.salvo.dto.response;

import lombok.Data;

@Data
public class PlayerScoreDTO {

    private String email;
    private Integer score;
    private Integer won;
    private Integer tied;
    private Integer lost;
}
