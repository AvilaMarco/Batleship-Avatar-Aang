package dev.racnar.shipbender.service.intereface;

import dev.racnar.shipbender.models.GamePlayer;

public interface IGamePlayerService {

    GamePlayer save ( GamePlayer gamePlayer );

    GamePlayer getGamePlayerBy ( Long playerId, Long gameId );
}
