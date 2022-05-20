package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.dto.GamePlayerDTO;
import com.codeblockacademy.shipbender.models.GamePlayer;

public interface IGamePlayerService {

    GamePlayerDTO save ( GamePlayer gamePlayer );

    GamePlayerDTO getGamePlayerBy ( Long playerId, Long gameId );
}
