package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Game findByDirection(String direction);

    @Query("FROM Game g WHERE g.direction = :direction AND g.endGame = false ")
    Optional<Game> getGameByDirection(String direction);
}
