package dev.racnar.shipbender.repository;

import dev.racnar.shipbender.models.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    Optional<GamePlayer> findByPlayerIdAndGameId ( Long gameId, Long playerId );
}
