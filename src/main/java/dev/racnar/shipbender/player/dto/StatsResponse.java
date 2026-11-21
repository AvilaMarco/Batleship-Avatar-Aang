package dev.racnar.shipbender.player.dto;

import dev.racnar.shipbender.player.domain.PlayerStats;

public record StatsResponse(int score, int won, int tied, int lost, double winRate) {

    public static StatsResponse from ( PlayerStats s ) {
        return new StatsResponse(s.score(), s.won(), s.tied(), s.lost(), s.winRate());
    }
}
