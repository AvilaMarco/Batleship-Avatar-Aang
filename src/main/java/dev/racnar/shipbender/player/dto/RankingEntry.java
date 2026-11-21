package dev.racnar.shipbender.player.dto;

import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.player.domain.PlayerStats;

public record RankingEntry(PlayerSummary player, StatsResponse stats) {

    public static RankingEntry from ( Player p ) {
        return new RankingEntry(PlayerSummary.from(p), StatsResponse.from(PlayerStats.of(p.getScores())));
    }
}
