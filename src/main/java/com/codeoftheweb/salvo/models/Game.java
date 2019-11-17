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
    private String ubicacion;
    private String direccion;

    //relaciones
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<GamePlayer> gamePlayers = new HashSet<>();
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<Score> scores = new HashSet<>();

    //constructores
    public Game(){}

    public Game(int horaCreation){
    this.creationDate = LocalDateTime.now().plusHours(horaCreation);
    }

    public Game(int horaCreation,String ubicacion,String direccion){
    this.creationDate = LocalDateTime.now().plusHours(horaCreation);
    this.ubicacion = ubicacion;
    this.direccion = direccion;
    }

    //getters and setters
    public String getubicacion() {
        return ubicacion;
    }

    public void setubicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    @JsonIgnore
    public Set<Player> getPlayers(){
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
        dto.put("ubicacion", this.ubicacion);
        dto.put("direccion", this.direccion);
        dto.put("gameplayers",this.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO));
        return dto;
    }
    //method static(pertenecen a la clase)
}
