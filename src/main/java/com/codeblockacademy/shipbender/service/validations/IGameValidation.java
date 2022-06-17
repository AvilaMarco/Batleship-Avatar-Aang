package com.codeblockacademy.shipbender.service.validations;

import com.codeblockacademy.shipbender.models.Game;

public interface IGameValidation {
    void exists ( Long gameId );

    void notExists ( String direction );

    void isNotFull ( Game game );

    void notContainsThePlayer ( Game game, Long playerId );

    void containsThePlayer ( Long gameId, Long playerId );
}
