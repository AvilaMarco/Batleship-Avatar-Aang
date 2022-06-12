package com.codeblockacademy.shipbender.dto.response;

import com.codeblockacademy.shipbender.enums.GameStatus;
import com.codeblockacademy.shipbender.enums.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusGameDTO {
    GameStatus   game;
    PlayerStatus host;
    PlayerStatus client;
}
