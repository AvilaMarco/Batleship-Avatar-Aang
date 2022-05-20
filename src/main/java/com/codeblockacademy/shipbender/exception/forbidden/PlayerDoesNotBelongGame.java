package com.codeblockacademy.shipbender.exception.forbidden;

public class PlayerDoesNotBelongGame extends ForbiddenException {
    public PlayerDoesNotBelongGame ( Long playerId, Long gameId ) {
        super("The player with id <" + playerId + "> does not belong to the game with id <" + gameId + ">", "Player Does not Belong the Game");
    }
}
