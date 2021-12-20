package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private LocalDateTime created;
    private String location;
    private String direction;

    //relaciones
    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Score> scores = new HashSet<>();

    //constructores
    public Game() {
    }

    public Game(int horaCreation) {
        this.created = LocalDateTime.now().plusHours(horaCreation);
    }

    public Game(int horaCreation, String location, String direction) {
        this.created = LocalDateTime.now().plusHours(horaCreation);
        this.location = location;
        this.direction = direction;
    }

    //getters and setters
    public String getubicacion() {
        return location;
    }

    public long getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    @JsonIgnore
    public Set<Player> getPlayers() {
        return this.gamePlayers.stream().map(GamePlayer::getPlayer).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<GamePlayer> getGamePlayers() {
        return this.gamePlayers;
    }

    //dto(date transfer object)
    public Map<String, Object> gamesDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("created", this.created);
        dto.put("ubicacion", this.location);
        dto.put("direccion", this.direction);
        dto.put("gameplayers", this.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO));
        return dto;
    }
}
