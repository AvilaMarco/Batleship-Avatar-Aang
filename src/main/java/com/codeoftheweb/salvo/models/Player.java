package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String name;
    private String email;
    private String password;
    private String nation;

    // Relations
    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Score> scores = new HashSet<>();

    //constructores

    public Player(String first, String email, String password, String nation) {
        this.name = first;
        this.email = email;
        this.password = password;
        this.nation = nation;
    }

    public Player(String first, String email, String password) {
        this.name = first;
        this.email = email;
        this.password = password;
    }


    //getter and setters
    public String getPassword() {
        return password;
    }

    public String getNation() {
        return this.nation;
    }

    public void setNation(String nacion) {
        this.nation = nacion;
    }


    public String getName() {
        return name;
    }

    public void setName(String firstName) {
        this.name = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //otros metodos
    @JsonIgnore
    public Score getScore(Game game) {
        return this.scores.stream().filter(e -> e.getGame().getId() == game.getId()).findFirst().orElse(null);
    }

    @JsonIgnore
    public Set<Score> getScores() {

        return scores;
    }

    public long getId() {
        return id;
    }

    @JsonIgnore
    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    //dto
    public Map<String, Object> playerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("name", this.name);
        dto.put("email", this.email);
        dto.put("nacion", this.nation);
        return dto;
    }

    public Map<String, Object> playerScoreDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("name", this.name);
        dto.put("email", this.email);
        dto.put("scores", this.scores.stream().map(Score::getScore));
        return dto;
    }
}