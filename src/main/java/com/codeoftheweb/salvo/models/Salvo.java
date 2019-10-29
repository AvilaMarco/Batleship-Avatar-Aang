package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private int turn;

    //relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    private List<String> salvoLocations;

    //constructor
    public Salvo(){}

    public Salvo(List<String> salvoLocation, int turn){
        this.salvoLocations = salvoLocation;
        this.turn = turn;
    }

    //getter and setters
    public void setGamePlayer(GamePlayer GamePlayer){
        this.gamePlayer = GamePlayer;
    }

    public int getTurn(){
        return this.turn;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    //otros metodos
    @JsonIgnore
    public long getIdPlayer(){
        return this.gamePlayer.getPlayer().getId();
    }

    @JsonIgnore
    public List<String> goodShoot(List<String> shoots){
        GamePlayer gp =  this.gamePlayer.getGame().getGamePlayers().stream().filter(gamep-> gamep.getId()!=this.id).findFirst().orElse(null);
        if (gp != null && shoots.size()!=0){
            List<String> positionShips = new ArrayList<>();
            List<String> niceshoots = new ArrayList<>();
            gp.getShips().forEach(e->positionShips.addAll(e.getShipLocations()));
            return shoots.stream().filter(s-> positionShips.stream().anyMatch(p->p.equals(s))).collect(Collectors.toList());
        }else {
            return null;
        }
    }

    @JsonIgnore
    public List<Ship> shipsDead(){
        List<String> salvos = new ArrayList<>();
        this.gamePlayer.getSalvos().stream().forEach(s->salvos.addAll(s.salvoLocations));
        this.gamePlayer.getShips().stream().filter(s-> s.getShipLocations().stream().allMatch(l->salvos.stream().anyMatch()));
    }
    //dto
    public Map<String, Object> salvoDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn",this.turn);
        dto.put("player", this.getIdPlayer());
        dto.put("nice_shoot",this.goodShoot(this.salvoLocations));
        dto.put("ships_dead",this.shipsDead());
        dto.put("locations", this.salvoLocations);
        return dto;
    }
}
