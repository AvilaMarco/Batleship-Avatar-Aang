package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity //le dice a spring que cree una tabla de la clase
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String firstName;
    private String lastName;
    private String correo;
    //mappedBy="player" la clase muchos tiene una variable con este nombre
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<GamePlayer> gamePlayers = new HashSet<>();
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<Score> score = new HashSet<>();

    public Player() { }

    public Player(String first, String last, String email) {
        this.firstName = first;
        this.lastName = last;
        this.correo = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String email) { this.correo = email;}

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Score> getScore() {
        return score;
    }

    public long getId() {
        return id;
    }

    @JsonIgnore
    public Set<GamePlayer> getGamePlayers(){return gamePlayers;}

    //dto
    public Map<String, Object> playerDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id",this.id);
        dto.put("complete_name", this.firstName + ' '+this.lastName );
        dto.put("email", this.correo);
        return dto;
    }
}