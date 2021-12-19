package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.models.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
}
