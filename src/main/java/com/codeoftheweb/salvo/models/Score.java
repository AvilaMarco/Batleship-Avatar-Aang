package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private int score;
    private LocalDateTime finishDate;

    //relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    //constructores
    public Score() {
    }

    public Score(Player jugador, Game juego,int puntaje,LocalDateTime finishDate) {
        this.player = jugador;
        this.game = juego;
        this.score = puntaje;
        this.finishDate = finishDate;
    }

    //getter and setter
    @JsonIgnore
    public Game getGame() {
        return game;
    }
    @JsonIgnore
    public Player getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }
    @JsonIgnore
    public LocalDateTime getFinishDate() {
        return finishDate;
    }
}
