package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.models.GamePlayer;

public interface IGamePlayerService {

    GamePlayerDTO save ( GamePlayer gamePlayer );

    GamePlayerDTO getGamePlayerBy ( Long playerId, Long gameId );
}
