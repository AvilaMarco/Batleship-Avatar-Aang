package dev.racnar.shipbender.unit.domain;

import dev.racnar.shipbender.player.domain.PlayerStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerStatsTest {

    @Test
    void of_oneWinOneLoss_hasFiftyPercentWinRate () {
        assertThat(PlayerStats.of(List.of(3, 0))).isEqualTo(new PlayerStats(3, 1, 0, 1, 50.0));
    }

    @Test
    void of_noGames_isAllZero () {
        assertThat(PlayerStats.of(List.of())).isEqualTo(new PlayerStats(0, 0, 0, 0, 0.0));
    }

    @Test
    void of_oneTie_countsTiedAndScoreOne () {
        assertThat(PlayerStats.of(List.of(1))).isEqualTo(new PlayerStats(1, 0, 1, 0, 0.0));
    }
}
