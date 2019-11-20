package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

//le dice a spring que cree una tabla de la clase
@Entity 
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String firstName;
    private String correo;
    private String password;
    private String nacion;

    //relaciones
    //mappedBy="player" la clase muchos tiene una variable con este nombre
    //nombre del atributo que guarda la relacion en gameplayers
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER, cascade= CascadeType.ALL)
    private Set<Score> scores = new HashSet<>();

    //constructores
    public Player() { }

    public Player(String first, String email, String password,String nacion) {
        this.firstName = first;
        this.correo = email;
        this.password = password;
        this.nacion = nacion;
    }

    public Player(String first, String email, String password) {
        this.firstName = first;
        this.correo = email;
        this.password = password;
    }


    //getter and setters
    public String getPassword() {
        return password;
    }

    public String getNacion() {
        return this.nacion;
    }

    public void setNacion(String nacion) {
       this.nacion = nacion;
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

    //otros metodos
    @JsonIgnore
    public Score getScore(Game game) {
        return this.scores.stream().filter(e->e.getGame().getId() == game.getId()).findFirst().orElse(null);
    }

    @JsonIgnore
    public Set<Score> getScores() {

        return scores;
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
        dto.put("name", this.firstName);
        dto.put("email", this.correo);
        dto.put("nacion", this.nacion);
        return dto;
    }

    public Map<String, Object> playerScoreDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id",this.id);
        dto.put("name", this.firstName);
        dto.put("email", this.correo);
        dto.put("scores",this.scores.stream().map(Score::getScore));
        return dto;
    }
}