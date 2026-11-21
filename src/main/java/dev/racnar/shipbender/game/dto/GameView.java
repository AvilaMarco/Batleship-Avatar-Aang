package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.domain.GamePlayer;
import dev.racnar.shipbender.game.domain.GameStatus;
import dev.racnar.shipbender.player.dto.PlayerSummary;
import dev.racnar.shipbender.shared.NationType;

import java.util.List;

/** Vista de la partida relativa a quien pregunta. Solo lee las entidades, nunca las muta. */
public record GameView(long id, NationType nation, String location, GameStatus status, Integer turn,
                       Me me, Opponent opponent) {

    public record ShipView(String type, List<String> cells) {}

    public record Me(PlayerSummary player, boolean shipsPlaced, List<ShipView> ships) {}

    public record Opponent(PlayerSummary player, boolean shipsPlaced) {}

    public static GameView of ( Game g, long playerId ) {
        GamePlayer mine = g.participant(playerId);
        List<ShipView> ships = mine.getShips().stream()
          .map(s -> new ShipView(s.getType().name(), List.copyOf(s.getLocations()))).toList();
        Opponent opponent = g.opponentOf(playerId)
          .map(o -> new Opponent(PlayerSummary.from(o.getPlayer()), o.hasShips()))
          .orElse(null);
        return new GameView(g.getId(), g.getNation(), g.getLocation(), g.status(), g.getTurn(),
          new Me(PlayerSummary.from(mine.getPlayer()), mine.hasShips(), ships), opponent);
    }
}
