package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.domain.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<Ship, Long> {
}
