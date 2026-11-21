package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.domain.Salvo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalvoRepository extends JpaRepository<Salvo, Long> {
}
