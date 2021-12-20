package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
public class GamePlayer {
    //propiedades
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime joinDate;
    private String type;
    private String emote;
    private Boolean rematch;
    private long newGameId;
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
        this.type = jugador.getNation();
    }

    //setters and getters
    public long getnewGameId() {
        return this.newGameId;
    }

    public void setnewGameId(long newGameId) {
        this.newGameId = newGameId;
    }

    public Boolean getRematch() {
        return this.rematch;
    }

    public void setRematch(Boolean isRematch) {
        this.rematch = isRematch;
    }

    public String getEmote() {
        return this.emote;
    }

    public void setEmote(String emote) {
        this.emote = emote;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return this.type;
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
    public Object getScore() {
        Score score = this.player.getScore(this.game);
        if (score != null) {
            return score.getScore();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public boolean gamestard() {
        long migpid = this.getId();
        GamePlayer gpOpponent = this.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migpid).findFirst().orElse(null);
        if (gpOpponent != null) {
            return this.getGame().getGamePlayers().stream().allMatch(gp -> gp.getShips().size() == 5);
        } else {
            return false;
        }

    }

    @JsonIgnore
    public int getMyTurn() {
        return (this.getSalvos().size() + 1);
    }

    @JsonIgnore
    public int getTurnOpponent() {
        long migpid = this.getId();
        GamePlayer gpOpponent = this.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migpid).findFirst().orElse(null);
        if (gpOpponent != null) {
            return (gpOpponent.getSalvos().size() + 1);
        } else {
            return -1;
        }
    }

    @JsonIgnore
    public boolean gameover() {
        long migpid = this.getId();
        GamePlayer mygp = this;
        GamePlayer gpOpponent = this.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migpid).findFirst().orElse(null);
        if (gpOpponent != null) {
            //empate cuando mis disparos destruyen todos los barcos en el mismo turno que mi oponente hace lo mismo
            //de mi gp obtengo los barcos que destrui
            if (mygp.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5) && gpOpponent.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5)) {
                return true;
            } else
                return mygp.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5) || gpOpponent.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5);
        } else {
            return false;
        }
    }

    @JsonIgnore
    public String getEmoteOpponent() {
        long migpid = this.getId();
        GamePlayer gpOpponent = this.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migpid).findFirst().orElse(null);
        if (gpOpponent != null) {
            return gpOpponent.getEmote();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public Boolean getRematchOpponent() {
        long migpid = this.getId();
        GamePlayer gpOpponent = this.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migpid).findFirst().orElse(null);
        if (gpOpponent != null) {
            return gpOpponent.getRematch();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public Long getnewGameIdOpponent() {
        long migpid = this.getId();
        GamePlayer gpOpponent = this.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migpid).findFirst().orElse(null);
        if (gpOpponent != null) {
            return gpOpponent.getnewGameId();
        } else {
            return null;
        }
    }

    //dto
    public Map<String, Object> gamePlayerDTO() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", this.id);
        dto.put("tipo", this.type);
        dto.put("player", this.player.playerDTO());
        dto.put("Score", this.getScore());
        return dto;
    }

    public Map<String, Object> gameVIewDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getGame().getId());
        dto.put("ubicacion", this.getGame().getubicacion());
        dto.put("direccion", this.getGame().getDirection());
        dto.put("tipo", this.type);
        dto.put("created", this.joinDate);
        dto.put("Game_Started", this.gamestard());
        dto.put("Game_Over", this.gameover());
        dto.put("my_turn", this.getMyTurn());
        dto.put("Opponent_turn", this.getTurnOpponent());
        dto.put("my_emote", this.getEmote());
        dto.put("Opponent_emote", this.getEmoteOpponent());
        dto.put("my_rematch", this.getRematch());
        dto.put("Opponent_rematch", this.getRematchOpponent());
        if (this.getnewGameIdOpponent() != null) {
            dto.put("new_game", this.getnewGameIdOpponent());
        }
        dto.put("gamePlayers", this.game.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO));
        dto.put("ships", this.getShips().stream().map(Ship::shipsDTO));
        dto.put("salvoes", this.getGame().getGamePlayers().stream().flatMap(gp -> gp.getSalvos().stream().map(Salvo::salvoDTO)));
        return dto;
    }
}
