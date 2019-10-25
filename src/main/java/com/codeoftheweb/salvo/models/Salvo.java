package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    //dto
    public Map<String, Object> salvoDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn",this.turn);
        dto.put("player", this.getIdPlayer());
        dto.put("locations", this.salvoLocations);
        return dto;
    }
}
