package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.org.apache.xpath.internal.objects.XNull;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvo = new HashSet<>();

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
        this.salvo.add(salvo);
        salvo.setGamePlayer(this);
    }

    @JsonIgnore
    public Set<Ship> getShips() {
        return this.ships;
    }

    @JsonIgnore
    public Set<Salvo> getSalvo() {
        return this.salvo;
    }

    @JsonIgnore
    public GamePlayer getGameplayerOpponent(){
        GamePlayer gamePlayer = this;
        for(GamePlayer e : this.game.getGamePlayer()){
            if (e.getId() != this.getId()){
                gamePlayer = e;
            }
        }
        return gamePlayer;
    }
    //dto
    public Map<String, Object> gamePlayerDTO(){
        Map<String, Object> dto = new HashMap<>();
        dto.put("id",this.id);
        dto.put("player",this.player.playersDTO());
        return dto;
    }

    public Map<String, Object> gameVIewDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("created",this.joinDate);
        dto.put("gamePlayers", this.game.getPlayers().stream().map(Player::playersDTO));
        dto.put("ships",this.getShips().stream().map(Ship::shipsDTO));
        dto.put("salvoes",this.getSalvo().stream().map(Salvo::salvoDTO));
        dto.put("salvoes_opponent",this.getGameplayerOpponent().getSalvo().stream().map(Salvo::salvoDTO));
        return dto;
    }
}
