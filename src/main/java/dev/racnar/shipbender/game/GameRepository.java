package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("FROM Game g WHERE g.location = :location AND g.finishDate IS NULL")
    Optional<Game> getGameByLocation ( String location );

    List<Game> findAllByFinishDateIsNull ();
}
