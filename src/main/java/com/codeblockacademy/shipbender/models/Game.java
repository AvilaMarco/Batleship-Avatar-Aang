package com.codeblockacademy.shipbender.models;

import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.enums.GameStatus;
import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.enums.PlayerStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private NationType nation;
    private String     location;
    private Integer    turn;

    @CreationTimestamp
    private LocalDateTime created;

    private LocalDateTime finishDate;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<GamePlayer> gamePlayers = new ArrayList<>();

    public Game ( NationType nation, String location ) {
        this.nation   = nation;
        this.location = location;
        this.turn     = 1;
    }

    /* PUBLIC METHODS */

    public void updateStatusGpOf ( StatusGameDTO.StatusGameDTOBuilder builder ) {
        if (!this.isGameWaitingPlayer()) {
            if (this.isClientHasShips()) {
                builder.client(PlayerStatus.WITH_SHIPS);
            } else {
                builder.client(PlayerStatus.WITHOUT_SHIPS);
            }
        } else {
            builder.client(PlayerStatus.WAITING);
        }
        if (this.isHostHasShips()) {
            builder.host(PlayerStatus.WITH_SHIPS);
        } else {
            builder.host(PlayerStatus.WITHOUT_SHIPS);
        }
    }

    public void updateStatusGameOf ( StatusGameDTO.StatusGameDTOBuilder builder ) {
        if (this.isGameWaitingPlayer() || this.isGameWaitingShips()) {
            builder.game(GameStatus.CREATED);
        } else if (this.isGameFinish()) {
            builder.game(GameStatus.FINISH);
        } else {
            builder.game(GameStatus.IN_GAME);
        }
    }

    public void finishDate () {
        this.finishDate = LocalDateTime.now();
    }

    public boolean containsPlayer ( Long playerId ) {
        return gamePlayers.stream()
          .map(GamePlayer::getPlayer)
          .anyMatch(p -> p.getId() == playerId);
    }

    public boolean isGameFinish () {
        return gamePlayers.stream()
          .allMatch(gp -> gp.getScore() != null);
    }

    public boolean isFullGame () {
        return gamePlayers.size() == 2;
    }

    /* UTILS */
    private boolean isGameWaitingPlayer () {
        return gamePlayers.size() == 1;
    }

    private boolean isGameWaitingShips () {
        return !gamePlayers.stream()
          .allMatch(GamePlayer::hasShips);
    }

    private boolean isClientHasShips () {
        return gamePlayers.get(1)
          .hasShips();
    }

    private boolean isHostHasShips () {
        return gamePlayers.get(0)
          .hasShips();
    }
}
