package com.codeoftheweb.salvo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreStatsDTO {

    private Integer score;
    private Integer won;
    private Integer tied;
    private Integer lost;
    private Double  winRate;

    public void plusScore ( Integer score ) {
        this.score += score;
    }

    public void plusWon () {
        this.won++;
    }

    public void plusLost () {
        this.lost++;
    }

    public void plusTied () {
        this.tied++;
    }
}
