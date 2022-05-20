package com.codeblockacademy.shipbender.repository;

import com.codeblockacademy.shipbender.models.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<Ship, Long> {
}
