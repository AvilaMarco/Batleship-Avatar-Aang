package dev.racnar.shipbender.repository;

import dev.racnar.shipbender.models.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<Ship, Long> {
}
