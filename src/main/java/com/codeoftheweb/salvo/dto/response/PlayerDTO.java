package com.codeoftheweb.salvo.dto.response;

import com.codeoftheweb.salvo.enums.NationType;
import lombok.Data;

@Data
public class PlayerDTO {

    private String name;
    private String email;
    private NationType nation;

    private Integer score;
    private Integer won;
    private Integer tied;
    private Integer lost;
}
