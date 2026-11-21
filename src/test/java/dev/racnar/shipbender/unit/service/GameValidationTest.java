package dev.racnar.shipbender.unit.service;

import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.shared.error.conflict.GameAlreadyExistsException;
import dev.racnar.shipbender.shared.error.conflict.GameIsFullException;
import dev.racnar.shipbender.shared.error.conflict.PlayerAlreadyInGameException;
import dev.racnar.shipbender.shared.error.not_found.GameNotFoundException;
import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.domain.GamePlayer;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.game.GameRepository;
import dev.racnar.shipbender.game.GameValidation;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameValidationTest {

    final GameRepository  repository = mock(GameRepository.class);
    final GameValidation  validation = new GameValidation(repository, new ModelMapper());

    private Game gameWithPlayers ( long... playerIds ) {
        Game game = new Game(NationType.WATER, "loc");
        game.setId(1L);
        for (long id : playerIds) {
            Player p = new Player();
            p.setId(id);
            game.getGamePlayers().add(new GamePlayer(p, game));
        }
        return game;
    }

    @Test
    void exists_passesWhenGameExists_andThrowsWhenNot () {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(2L)).thenReturn(false);

        assertDoesNotThrow(() -> validation.exists(1L));
        assertThrows(GameNotFoundException.class, () -> validation.exists(2L));
    }

    @Test
    void notExists_passesWhenLocationFree_andThrowsWhenTaken () {
        when(repository.getGameByLocation("free")).thenReturn(Optional.empty());
        when(repository.getGameByLocation("taken")).thenReturn(Optional.of(new Game()));

        assertDoesNotThrow(() -> validation.notExists("free"));
        assertThrows(GameAlreadyExistsException.class, () -> validation.notExists("taken"));
    }

    @Test
    void isNotFull_passesWithOnePlayer_andThrowsWithTwo () {
        assertDoesNotThrow(() -> validation.isNotFull(gameWithPlayers(1L)));
        assertThrows(GameIsFullException.class, () -> validation.isNotFull(gameWithPlayers(1L, 2L)));
    }

    @Test
    void notContainsThePlayer_passesWhenAbsent_andThrowsWhenPresent () {
        Game game = gameWithPlayers(1L);

        assertDoesNotThrow(() -> validation.notContainsThePlayer(game, 2L));
        assertThrows(PlayerAlreadyInGameException.class, () -> validation.notContainsThePlayer(game, 1L));
    }

    @Test
    void containsThePlayer_passesWhenPresent_andThrowsWrongNamedExceptionWhenAbsent () {
        when(repository.findById(1L)).thenReturn(Optional.of(gameWithPlayers(1L)));

        assertDoesNotThrow(() -> validation.containsThePlayer(1L, 1L));
        // nombre incorrecto pero es el comportamiento actual
        assertThrows(PlayerAlreadyInGameException.class, () -> validation.containsThePlayer(1L, 2L));
    }

    @Test
    void containsThePlayer_withUnknownGame_throwsGameNotFound () {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> validation.containsThePlayer(9L, 1L));
    }

    @Test
    void containsPlayer_withIdsAbove127_findsThePlayer () {
        // Player.id es long primitivo: el == de Game.java:82 desempaqueta y NO falla (el bug supuesto no se reproduce)
        Game game = gameWithPlayers(1000L);

        assertTrue(game.containsPlayer(Long.valueOf(1000L)));
    }
}
