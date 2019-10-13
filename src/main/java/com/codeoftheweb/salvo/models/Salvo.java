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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    private List<String> salvoLocation;

    private int turn;

    public Salvo(){}

    public Salvo(List<String> salvoLocation, int turn){
        this.salvoLocation = salvoLocation;
        this.turn = turn;
    }

    public void setGamePlayer(GamePlayer GamePlayer){
        this.gamePlayer = GamePlayer;
    }

    public int getTurn(){
        return this.turn;
    }

    @JsonIgnore
    public long getIdPlayer(){
        return this.gamePlayer.getPlayer().getId();
    }

    public List<String> getSalvoLocation() {
        return salvoLocation;
    }

    //dto
    public Map<String, Object> salvoDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn",this.turn);
        dto.put("player", this.getIdPlayer());
        dto.put("locations", this.salvoLocation);
        return dto;
    }
}
