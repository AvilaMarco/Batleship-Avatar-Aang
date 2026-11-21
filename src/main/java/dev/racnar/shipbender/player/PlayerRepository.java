package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByEmail ( String name );

    boolean existsByEmail ( String email );

    @Query("SELECT DISTINCT p FROM Player p JOIN p.gamePlayers gp WHERE gp.score IS NOT NULL")
    List<Player> rankedPlayer ();
}