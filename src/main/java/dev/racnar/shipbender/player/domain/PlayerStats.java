package dev.racnar.shipbender.player.domain;

import java.util.List;

public record PlayerStats(int score, int won, int tied, int lost, double winRate) {

    public static PlayerStats of ( List<Integer> scores ) {
        int won  = (int) scores.stream().filter(s -> s == 3).count();
        int tied = (int) scores.stream().filter(s -> s == 1).count();
        int lost = (int) scores.stream().filter(s -> s == 0).count();
        int sum  = scores.stream().mapToInt(Integer::intValue).sum();
        double winRate = scores.isEmpty() ? 0.0 : won * 100.0 / scores.size();
        return new PlayerStats(sum, won, tied, lost, winRate);
    }
}
