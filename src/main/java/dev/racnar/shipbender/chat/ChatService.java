package dev.racnar.shipbender.chat;

import dev.racnar.shipbender.game.GameRepository;
import dev.racnar.shipbender.game.event.GameEvent;
import dev.racnar.shipbender.player.PlayerService;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final GameRepository            games;
    private final PlayerService             playerService;
    private final ApplicationEventPublisher events;

    public ChatService ( GameRepository games, PlayerService playerService, ApplicationEventPublisher events ) {
        this.games         = games;
        this.playerService = playerService;
        this.events        = events;
    }

    /** No persiste: valida que sea participante y publica el evento. */
    @Transactional(readOnly = true)
    public void sendEmote ( String email, long gameId, String emote ) {
        long playerId = playerService.getPlayer(email).getId();
        games.findById(gameId)
          .orElseThrow(() -> new ShipbenderException(ErrorCode.GAME_NOT_FOUND, "No existe la partida " + gameId))
          .participant(playerId);
        events.publishEvent(new GameEvent.EmoteSent(gameId, playerId, emote));
    }
}
