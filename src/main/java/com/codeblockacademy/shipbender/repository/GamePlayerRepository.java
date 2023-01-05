package com.codeblockacademy.shipbender.repository;

import com.codeblockacademy.shipbender.models.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    Optional<GamePlayer> findByPlayerIdAndGameId ( Long gameId, Long playerId );
}
