package dev.racnar.shipbender.player.dto;

import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.player.domain.PlayerStats;
import dev.racnar.shipbender.shared.NationType;

public record PlayerProfile(long id, String name, String email, NationType nation, StatsResponse stats) {

    public static PlayerProfile from ( Player p ) {
        return new PlayerProfile(p.getId(), p.getName(), p.getEmail(), p.getNation(),
          StatsResponse.from(PlayerStats.of(p.getScores())));
    }
}
