package com.codeblockacademy.shipbender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmoteDTO {

    private Long   gamePlayerId;
    private String emote;
}
