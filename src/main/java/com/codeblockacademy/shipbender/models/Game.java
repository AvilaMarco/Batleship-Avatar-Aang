package com.codeblockacademy.shipbender.models;

import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private NationType nation;
    private String     location;

    @CreationTimestamp
    private LocalDateTime created;

    private LocalDateTime finishDate;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<GamePlayer> gamePlayers = new ArrayList<>();

    public Game ( NationType nation, String location ) {
        this.nation   = nation;
        this.location = location;
    }

    /* PUBLIC METHODS */

    public void updateStatusGpOf ( StatusGameDTO.StatusGameDTOBuilder builder ) {
        if (!this.isGameWaiting()) {
            builder
              .gpPlayerOneId(getGPCreator())
              .gpPlayerTwoId(getGPJoined());
        } else {
            builder
              .gpPlayerOneId(getGPCreator());
        }
    }

    public void updateStatusGameOf ( StatusGameDTO.StatusGameDTOBuilder builder ) {
        // ToDo: created enum for status game
        if (this.isGameStart()) {
            builder.status("GAME_START");
        } else if (this.isGameWatingShips()) {
            builder.status("WAITING_SHIPS");
        } else if (this.isGameWaiting()) {
            builder.status("WAITING");
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
    private boolean isGameStart () {
        return gamePlayers.stream()
          .noneMatch(gp -> gp.getShips()
            .isEmpty());
    }

    private boolean isGameWatingShips () {
        return gamePlayers.stream()
          .anyMatch(gp -> gp.getShips()
            .isEmpty());
    }

    private boolean isGameWaiting () {
        return gamePlayers.size() == 1;
    }

    private Long getGPCreator () {
        // ! ToDo: review this content
        return gamePlayers.get(0)
          .getId();
    }

    private Long getGPJoined () {
        // ! ToDo: review this content
        return gamePlayers.get(1)
          .getId();
    }
}
