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
        long migp = this.gamePlayer.getId();
        GamePlayer gp =  this.gamePlayer.getGame().getGamePlayers().stream().filter(gamep-> gamep.getId()!=migp).findFirst().orElse(null);
        System.out.println(gp);
        if (gp != null && shoots.size()==5){
            List<String> positionShips = gp.getShips().stream().flatMap(e -> e.getShipLocations().stream().map(l->l)).collect(Collectors.toList());
            System.out.println(positionShips);
            return shoots.stream().filter(s-> positionShips.stream().anyMatch(p->p.equals(s))).collect(Collectors.toList());
        }else {
            return null;
        }
    }

    @JsonIgnore
    public List<Ship> shipsDead(){
        List<String> salvosposition = new ArrayList<>();
//        this.gamePlayer.getSalvos().forEach(salvo->salvosposition.addAll(salvo.salvoLocations));
        this.gamePlayer.getSalvos().stream().filter(salvo -> salvo.getTurn() <= this.getTurn()).forEach(salvo->salvosposition.addAll(salvo.salvoLocations));
        long migp = this.gamePlayer.getId();
        GamePlayer gp =  this.gamePlayer.getGame().getGamePlayers().stream().filter(gamep-> gamep.getId()!=migp).findFirst().orElse(null);
        if (gp !=null){
            List<Ship> ships = new ArrayList<>(gp.getShips());
            List<Ship> shipsDead = new ArrayList<>(ships.stream().filter(s -> s.getShipLocations().stream().allMatch(position -> salvosposition.stream().anyMatch(sp -> sp.equals(position)))).collect(Collectors.toList()));
            if (shipsDead.size()!=0){
                return shipsDead;
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
    //dto
    public Map<String, Object> salvoDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn",this.turn);
        dto.put("player", this.getIdPlayer());
        dto.put("nice_shoot",this.goodShoot(this.salvoLocations));
        if (this.shipsDead() != null){
            dto.put("ships_dead",this.shipsDead().stream().map(Ship::shipstypeDTO).collect(Collectors.toList()));
        }else{
            dto.put("ships_dead",this.shipsDead());
        }
        dto.put("locations", this.salvoLocations);
        return dto;
    }
}
