package com.codeblockacademy.shipbender.unit.service;

import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.exception.conflict.GameAlreadyExistsException;
import com.codeblockacademy.shipbender.exception.conflict.GameIsFullException;
import com.codeblockacademy.shipbender.exception.conflict.PlayerAlreadyInGameException;
import com.codeblockacademy.shipbender.exception.not_found.GameNotFoundException;
import com.codeblockacademy.shipbender.models.Game;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.repository.GameRepository;
import com.codeblockacademy.shipbender.service.validations.GameValidation;
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
