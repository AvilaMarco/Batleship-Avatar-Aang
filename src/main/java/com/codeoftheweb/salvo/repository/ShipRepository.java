package com.codeoftheweb.salvo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import com.codeoftheweb.salvo.models.Ship;

@RepositoryRestResource
public interface ShipRepository extends JpaRepository<Ship, Long> {
}
