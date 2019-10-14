package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import  java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Game {
    //propiedades
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private LocalDateTime creationDate;
    private String winner;
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<GamePlayer> gamePlayers = new HashSet<>();
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<Score> score = new HashSet<>();

    //constructores
    public Game(){}

    public Game(int horaCreation){
    this.creationDate = LocalDateTime.now().plusHours(horaCreation);
    }

    //getters and setters
    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public long getId() {
        return id;
    }

    //Método que busca en las blueCards asociadas con el conductor los vehículos que le pertenecen
    @JsonIgnore
    public Set<Player> getPlayers(){
        //lamamos a la propiedad blueCards que contiene a todos los blueCards relacionados con el conductor
        //la recorremos y por cada una llamamos a su getter de Vehicle, para almacenar cada vehículo
        //en la lista que devuelve el método.
        return this.gamePlayers.stream().map(GamePlayer::getPlayer).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<GamePlayer> getGamePlayers(){
        return this.gamePlayers;
    }

    //dto(date transfer object)
    public Map<String, Object> gamesDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("created",this.creationDate);
        dto.put("gameplayers",this.getGamePlayers().stream().map(e->e.gamePlayerDTO()));
        return dto;
    }
    //method static(pertenecen a la clase)
}
