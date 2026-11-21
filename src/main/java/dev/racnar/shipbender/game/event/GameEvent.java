package dev.racnar.shipbender.game.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.racnar.shipbender.game.domain.ShipType;
import dev.racnar.shipbender.player.dto.PlayerSummary;

import java.util.List;
import java.util.Map;

/** Eventos de /topic/games/{id} (ADR 004 Anexo B); nunca llevan celdas de barcos. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = GameEvent.PlayerJoined.class, name = "PLAYER_JOINED"),
  @JsonSubTypes.Type(value = GameEvent.ShipsPlaced.class, name = "SHIPS_PLACED"),
  @JsonSubTypes.Type(value = GameEvent.GameStarted.class, name = "GAME_STARTED"),
  @JsonSubTypes.Type(value = GameEvent.EmoteSent.class, name = "EMOTE"),
  @JsonSubTypes.Type(value = GameEvent.SalvoFired.class, name = "SALVO_FIRED"),
  @JsonSubTypes.Type(value = GameEvent.TurnResolved.class, name = "TURN_RESOLVED"),
  @JsonSubTypes.Type(value = GameEvent.GameFinished.class, name = "GAME_FINISHED")
})
public sealed interface GameEvent {

    long gameId ();

    record PlayerJoined ( long gameId, PlayerSummary player ) implements GameEvent {}

    record ShipsPlaced ( long gameId, long playerId ) implements GameEvent {}

    record GameStarted ( long gameId, int turn ) implements GameEvent {}

    record EmoteSent ( long gameId, long playerId, String emote ) implements GameEvent {}

    /** Sin celdas: el rival no debe saber qué disparaste hasta que se resuelva el turno. */
    record SalvoFired ( long gameId, long playerId, int turn ) implements GameEvent {}

    /** Solo hits/misses/hundidos de lo disparado; nunca posiciones de barcos no hundidos. */
    record TurnResolved ( long gameId, int turn, List<PlayerResult> results ) implements GameEvent {}

    record PlayerResult ( long playerId, List<String> hits, List<String> misses, List<ShipType> sunk ) {}

    record GameFinished ( long gameId, Long winnerId, Map<String, Integer> scores ) implements GameEvent {}
}
