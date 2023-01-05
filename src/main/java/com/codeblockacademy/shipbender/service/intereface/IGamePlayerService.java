package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.models.GamePlayer;

public interface IGamePlayerService {

    GamePlayer save ( GamePlayer gamePlayer );

    GamePlayer getGamePlayerBy ( Long playerId, Long gameId );
}
