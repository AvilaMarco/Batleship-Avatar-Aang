package dev.racnar.shipbender.game.domain;

import dev.racnar.shipbender.player.domain.Player;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
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
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvos = new HashSet<>();

    //constructores

    public GamePlayer ( Player jugador, Game juego ) {
        this.player   = jugador;
        this.game     = juego;
        this.joinDate = LocalDateTime.now();
    }

    public void addSalvo ( Salvo salvo ) {
        this.salvos.add(salvo);
        salvo.setGamePlayer(this);
    }

    public boolean hasShips () {
        return !ships.isEmpty();
    }
}
