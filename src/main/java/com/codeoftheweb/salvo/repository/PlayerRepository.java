package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByEmail ( @Param("name") String name );

    @Query("FROM Player p WHERE p.email = :email")
    Optional<Player> existsPlayer ( String email );

    @Query("FROM Player p JOIN p.gamePlayers gp WHERE gp.score IS NOT NULL")
    List<Player> rankedPlayer ();
}