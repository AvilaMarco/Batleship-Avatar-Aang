package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.models.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
	GamePlayer findById(@Param("id") String id);
}
