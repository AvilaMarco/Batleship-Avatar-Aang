package com.codeblockacademy.shipbender.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime joinDate;
    private String        emote;
    private Integer       score;

    // Relations
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "player_id")
    private Player player;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "gamePlayer_id")
    private Set<Ship> ships;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvos = new HashSet<>();

    //constructores

    public GamePlayer ( Player jugador, Game juego ) {
        this.player   = jugador;
        this.game     = juego;
        this.joinDate = LocalDateTime.now();
    }

    public void setShips ( Set<Ship> ships ) {
        if (this.ships == null) {
            this.ships = new HashSet<>();
        }
        this.ships.addAll(ships);
    }

    public void addSalvo ( Salvo salvo ) {
        this.salvos.add(salvo);
        salvo.setGamePlayer(this);
    }

    public boolean hasShips () {
        return !ships.isEmpty();
    }

    public void emptyShips () {
        this.ships = new HashSet<>();
    }

//    @JsonIgnore
//    public boolean gamestard () {
//        long migpid = this.getId();
//        GamePlayer gpOpponent = this.getGame()
//          .getGamePlayers()
//          .stream()
//          .filter(gamep -> gamep.getId() != migpid)
//          .findFirst()
//          .orElse(null);
//        if (gpOpponent != null) {
//            return this.getGame()
//              .getGamePlayers()
//              .stream()
//              .allMatch(gp -> gp.getShips()
//                .size() == 5);
//        } else {
//            return false;
//        }
//
//    }
//
//    @JsonIgnore
//    public int getMyTurn () {
//        return ( this.getSalvos()
//          .size() + 1 );
//    }
//
//    @JsonIgnore
//    public boolean gameover () {
//        long       migpid = this.getId();
//        GamePlayer mygp   = this;
//        GamePlayer gpOpponent = this.getGame()
//          .getGamePlayers()
//          .stream()
//          .filter(gamep -> gamep.getId() != migpid)
//          .findFirst()
//          .orElse(null);
//        if (gpOpponent != null) {
//            //empate cuando mis disparos destruyen todos los barcos en el mismo turno que mi oponente hace lo mismo
//            //de mi gp obtengo los barcos que destrui
//            if (mygp.getSalvos()
//              .stream()
//              .anyMatch(s -> s.shipsDead() != null && s.shipsDead()
//                .size() == 5) && gpOpponent.getSalvos()
//              .stream()
//              .anyMatch(s -> s.shipsDead() != null && s.shipsDead()
//                .size() == 5)) {
//                return true;
//            } else
//                return mygp.getSalvos()
//                  .stream()
//                  .anyMatch(s -> s.shipsDead() != null && s.shipsDead()
//                    .size() == 5) || gpOpponent.getSalvos()
//                  .stream()
//                  .anyMatch(s -> s.shipsDead() != null && s.shipsDead()
//                    .size() == 5);
//        } else {
//            return false;
//        }
//    }

}
