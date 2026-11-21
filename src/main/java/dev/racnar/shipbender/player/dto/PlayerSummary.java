package dev.racnar.shipbender.player.dto;

import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.shared.NationType;

public record PlayerSummary(long id, String name, NationType nation) {

    public static PlayerSummary from ( Player p ) {
        return new PlayerSummary(p.getId(), p.getName(), p.getNation());
    }
}
