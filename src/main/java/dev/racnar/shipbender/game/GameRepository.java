package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    boolean existsByLocationAndFinishDateIsNull ( String location );

    List<Game> findAllByFinishDateIsNull ();
}
