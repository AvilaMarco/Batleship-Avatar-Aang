package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.domain.GamePlayer;
import dev.racnar.shipbender.game.domain.GameStatus;
import dev.racnar.shipbender.game.domain.Salvo;
import dev.racnar.shipbender.game.domain.ShipType;
import dev.racnar.shipbender.player.dto.PlayerSummary;
import dev.racnar.shipbender.shared.NationType;

import java.util.Comparator;
import java.util.List;

/** Vista de la partida relativa a quien pregunta. Solo lee las entidades, nunca las muta. */
public record GameView(long id, NationType nation, String location, GameStatus status, Integer turn,
                       Me me, Opponent opponent) {

    public record ShipView(String type, List<String> cells) {}

    public record SalvoView(int turn, List<String> cells, List<String> hits) {}

    public record Me(PlayerSummary player, boolean shipsPlaced, List<ShipView> ships,
                     List<SalvoView> salvos, List<ShipType> sunk) {}

    public record Opponent(PlayerSummary player, boolean shipsPlaced,
                           List<SalvoView> salvos, List<ShipType> sunk) {}

    public static GameView of ( Game g, long playerId ) {
        GamePlayer mine = g.participant(playerId);
        List<ShipView> ships = mine.getShips().stream()
          .map(s -> new ShipView(s.getType().name(), List.copyOf(s.getLocations()))).toList();
        Opponent opponent = g.opponentOf(playerId)
          .map(o -> new Opponent(PlayerSummary.from(o.getPlayer()), o.hasShips(),
            salvos(o, mine), sunk(mine, o)))
          .orElse(null);
        List<SalvoView> mySalvos = g.opponentOf(playerId).map(o -> salvos(mine, o)).orElse(List.of());
        List<ShipType> mySunk = g.opponentOf(playerId).map(o -> sunk(o, mine)).orElse(List.of());
        return new GameView(g.getId(), g.getNation(), g.getLocation(), g.status(), g.getTurn(),
          new Me(PlayerSummary.from(mine.getPlayer()), mine.hasShips(), ships, mySalvos, mySunk), opponent);
    }

    /** Salvos de shooter; los hits solo se revelan cuando el turno ya se resolvió (el rival también disparó). */
    private static List<SalvoView> salvos ( GamePlayer shooter, GamePlayer target ) {
        return shooter.getSalvos().stream().sorted(Comparator.comparingInt(Salvo::getTurn))
          .map(s -> new SalvoView(s.getTurn(), List.copyOf(s.getLocations()),
            target.firedIn(s.getTurn()) ? s.getLocations().stream().filter(target::hasShipAt).toList() : List.of()))
          .toList();
    }

    /** Barcos de target hundidos por los salvos ya resueltos de shooter. */
    private static List<ShipType> sunk ( GamePlayer shooter, GamePlayer target ) {
        var resolved = shooter.getSalvos().stream().filter(s -> target.firedIn(s.getTurn()))
          .flatMap(s -> s.getLocations().stream()).collect(java.util.stream.Collectors.toSet());
        return target.getShips().stream().filter(s -> resolved.containsAll(s.getLocations()))
          .map(s -> s.getType()).sorted().toList();
    }
}
