package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.models.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    Optional<GamePlayer> findByPlayerIdAndGameId(Long gameId, Long playerId);
}
