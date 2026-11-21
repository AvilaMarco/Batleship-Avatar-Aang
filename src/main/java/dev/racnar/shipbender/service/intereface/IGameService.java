package dev.racnar.shipbender.service.intereface;

import dev.racnar.shipbender.dto.GameMapDTO;
import dev.racnar.shipbender.dto.response.GameMatchDTO;
import dev.racnar.shipbender.dto.response.StatusGameDTO;
import dev.racnar.shipbender.models.Game;

import java.util.List;

public interface IGameService {

    Game getGame ( Long id );

    GameMatchDTO getGameMatch ( Long id, Long gamePlayerId );

    List<GameMapDTO> getGames ();

    GameMapDTO save ( Game game );

    StatusGameDTO statusGame ( Long gameId );

}
