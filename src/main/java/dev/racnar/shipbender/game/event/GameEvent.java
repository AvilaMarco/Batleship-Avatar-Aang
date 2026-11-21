package dev.racnar.shipbender.game.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.racnar.shipbender.player.dto.PlayerSummary;

/** Eventos de /topic/games/{id} (ADR 004 Anexo B); nunca llevan celdas de barcos. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = GameEvent.PlayerJoined.class, name = "PLAYER_JOINED"),
  @JsonSubTypes.Type(value = GameEvent.ShipsPlaced.class, name = "SHIPS_PLACED"),
  @JsonSubTypes.Type(value = GameEvent.GameStarted.class, name = "GAME_STARTED"),
  @JsonSubTypes.Type(value = GameEvent.EmoteSent.class, name = "EMOTE")
})
public sealed interface GameEvent {

    long gameId ();

    record PlayerJoined ( long gameId, PlayerSummary player ) implements GameEvent {}

    record ShipsPlaced ( long gameId, long playerId ) implements GameEvent {}

    record GameStarted ( long gameId, int turn ) implements GameEvent {}

    record EmoteSent ( long gameId, long playerId, String emote ) implements GameEvent {}
}
