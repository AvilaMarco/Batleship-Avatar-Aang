package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.domain.GameStatus;
import dev.racnar.shipbender.player.dto.PlayerSummary;
import dev.racnar.shipbender.shared.NationType;

import java.util.List;

public record GameSummary(long id, NationType nation, String location, GameStatus status, List<PlayerSummary> players) {

    public static GameSummary of ( Game g ) {
        return new GameSummary(g.getId(), g.getNation(), g.getLocation(), g.status(),
          g.getGamePlayers().stream().map(gp -> PlayerSummary.from(gp.getPlayer())).toList());
    }
}
