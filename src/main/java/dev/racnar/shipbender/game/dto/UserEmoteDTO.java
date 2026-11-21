package dev.racnar.shipbender.game.dto;

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
