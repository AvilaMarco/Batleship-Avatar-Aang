package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class GamePlayer {
    //propiedades
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime joinDate;

    //relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvos = new HashSet<>();

    //constructores
    public GamePlayer() {
    }

    public GamePlayer(Player jugador, Game juego) {
        this.player = jugador;
        this.game = juego;
        this.joinDate = LocalDateTime.now();
    }

    //setters and getters
    public long getId() {
        return id;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Game getGame() {
        return this.game;
    }

    public LocalDateTime getCreationDate() {
        return joinDate;
    }

    public void addShip(Ship ship) {
        this.ships.add(ship);
        ship.setGamePlayer(this);
    }

    public void addSalvo(Salvo salvo) {
        this.salvos.add(salvo);
        salvo.setGamePlayer(this);
    }

    @JsonIgnore
    public Set<Ship> getShips() {
        return this.ships;
    }

    @JsonIgnore
    public Set<Salvo> getSalvos() {
        return this.salvos;
    }

    @JsonIgnore
    public Object getScore(){
        Score score = this.player.getScore(this.game);
        if(score != null){
            return score.getScore();
        }else{
            return null;
        }
    }

    //dto
    public Map<String, Object> gamePlayerDTO(){
        Map<String, Object> dto = new HashMap<>();
        dto.put("id",this.id);
        dto.put("player",this.player.playerDTO());
        dto.put("Score",this.getScore());
        return dto;
    }

    public Map<String, Object> gameVIewDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("created",this.joinDate);
        dto.put("gamePlayers", this.game.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO));
        dto.put("ships",this.getShips().stream().map(Ship::shipsDTO));
        dto.put("salvoes",this.getGame().getGamePlayers().stream().flatMap(gp -> gp.getSalvos().stream().map(Salvo::salvoDTO)));
        return dto;
    }
}
