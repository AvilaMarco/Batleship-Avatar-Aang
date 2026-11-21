package dev.racnar.shipbender.service.validations;

import dev.racnar.shipbender.models.Game;

public interface IGameValidation {
    void exists ( Long gameId );

    void notExists ( String direction );

    void isNotFull ( Game game );

    void notContainsThePlayer ( Game game, Long playerId );

    void containsThePlayer ( Long gameId, Long playerId );
}
