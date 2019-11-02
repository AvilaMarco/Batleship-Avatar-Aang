package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class GamePlayer {
    //propiedades
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime joinDate;

    //relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvos = new HashSet<>();

    //constructores
    public GamePlayer() {
    }

    public GamePlayer(Player jugador, Game juego) {
        this.player = jugador;
        this.game = juego;
        this.joinDate = LocalDateTime.now();
    }

    //setters and getters
    public long getId() {
        return id;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Game getGame() {
        return this.game;
    }

    public LocalDateTime getCreationDate() {
        return joinDate;
    }

    public void addShip(Ship ship) {
        this.ships.add(ship);
        ship.setGamePlayer(this);
    }

    public void addSalvo(Salvo salvo) {
        this.salvos.add(salvo);
        salvo.setGamePlayer(this);
    }

    @JsonIgnore
    public Set<Ship> getShips() {
        return this.ships;
    }

    @JsonIgnore
    public Set<Salvo> getSalvos() {
        return this.salvos;
    }

    @JsonIgnore
    public Object getScore(){
        Score score = this.player.getScore(this.game);
        if(score != null){
            return score.getScore();
        }else{
            return null;
        }
    }

    @JsonIgnore
    public boolean gamestard(){
        long migpid = this.getId();
        GamePlayer gpOpponent =  this.getGame().getGamePlayers().stream().filter(gamep-> gamep.getId()!=migpid).findFirst().orElse(null);
        if (gpOpponent!=null){
            return this.getGame().getGamePlayers().stream().allMatch(gp->gp.getShips().size()==5);
        }else{
            return false;
        }

    }

    @JsonIgnore
    public int getMyTurn(){
        return (this.getSalvos().size()+1);
    }
    @JsonIgnore
    public int getTurnOpponent(){
        long migpid = this.getId();
        GamePlayer gpOpponent =  this.getGame().getGamePlayers().stream().filter(gamep-> gamep.getId()!=migpid).findFirst().orElse(null);
        if (gpOpponent!=null){
            return (gpOpponent.getSalvos().size()+1);
        }else {
            return -1;
        }
    }
    @JsonIgnore
    public boolean gameover(){
        long migpid = this.getId();
        GamePlayer mygp =  this;
        GamePlayer gpOpponent =  this.getGame().getGamePlayers().stream().filter(gamep-> gamep.getId()!=migpid).findFirst().orElse(null);
        if (gpOpponent!=null){
            //empate cuando mis disparos destruyen todos los barcos en el mismo turno que mi oponente hace lo mismo
            //de mi gp obtengo los barcos que destrui
            if (mygp.getSalvos().stream().anyMatch(salvo->salvo.shipsDead().size()==5) && gpOpponent.getSalvos().stream().anyMatch(salvo->salvo.shipsDead().size()==5)){
                return true;
            }else if (mygp.getSalvos().stream().anyMatch(salvo->salvo.shipsDead().size()==5) || gpOpponent.getSalvos().stream().anyMatch(salvo->salvo.shipsDead().size()==5)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
//        return false;
    }

    //dto
    public Map<String, Object> gamePlayerDTO(){
        Map<String, Object> dto = new HashMap<>();
        dto.put("id",this.id);
        dto.put("player",this.player.playerDTO());
        dto.put("Score",this.getScore());
        return dto;
    }

    public Map<String, Object> gameVIewDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("created",this.joinDate);
        dto.put("Game_Started",this.gamestard());
        dto.put("Game_Over",this.gameover());
        dto.put("my_turn",this.getMyTurn());
        dto.put("Opponent_turn",this.getTurnOpponent());
        dto.put("gamePlayers", this.game.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO));
        dto.put("ships",this.getShips().stream().map(Ship::shipsDTO));
        dto.put("salvoes",this.getGame().getGamePlayers().stream().flatMap(gp -> gp.getSalvos().stream().map(Salvo::salvoDTO)));
        return dto;
    }
}
