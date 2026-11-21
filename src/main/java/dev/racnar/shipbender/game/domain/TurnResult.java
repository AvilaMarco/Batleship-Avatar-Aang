package dev.racnar.shipbender.game.domain;

import java.util.List;

public record TurnResult(int turn, List<PlayerTurn> results, boolean finished, Long winnerId) {

    /** hits/misses/sunk de lo que disparó {@code playerId} en este turno (sunk = hundidos en este turno). */
    public record PlayerTurn(long playerId, List<Cell> hits, List<Cell> misses, List<ShipType> sunk) {
    }
}
