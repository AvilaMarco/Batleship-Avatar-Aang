package com.codeblockacademy.shipbender.unit.service;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.response.GameCreatedDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.exception.conflict.GameAlreadyExistsException;
import com.codeblockacademy.shipbender.exception.conflict.GameIsFullException;
import com.codeblockacademy.shipbender.models.Game;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.service.MatchService;
import com.codeblockacademy.shipbender.service.intereface.IGamePlayerService;
import com.codeblockacademy.shipbender.service.intereface.IGameService;
import com.codeblockacademy.shipbender.service.intereface.IPlayerService;
import com.codeblockacademy.shipbender.service.validations.IGameValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    IPlayerService playerService;
    @Mock
    IGameService gameService;
    @Mock
    IGamePlayerService gamePlayerService;
    @Mock
    IGameValidation gameValidation;

    @InjectMocks
    MatchService matchService;

    // PlayerService.getPlayerAuthenticated ignora el parametro
    final Authentication auth = mock(Authentication.class);

    @Test
    void joinGame_withFreeGame_savesGamePlayerAndReturnsIds () {
        Player player = new Player();
        player.setId(7L);
        Game game = new Game(NationType.WATER, "loc");
        game.setId(3L);
        when(playerService.getPlayerAuthenticated(auth)).thenReturn(player);
        when(gameService.getGame(3L)).thenReturn(game);

        GameCreatedDTO result = matchService.joinGame(auth, 3L);

        assertEquals(3L, result.getGameId());
        verify(gameValidation).isNotFull(game);
        verify(gamePlayerService).save(any(GamePlayer.class));
    }

    @Test
    void joinGame_withFullGame_propagatesErrorAndDoesNotSave () {
        Game game = new Game(NationType.WATER, "loc");
        game.setId(3L);
        when(playerService.getPlayerAuthenticated(auth)).thenReturn(new Player());
        when(gameService.getGame(3L)).thenReturn(game);
        doThrow(new GameIsFullException(3L)).when(gameValidation).isNotFull(game);

        assertThrows(GameIsFullException.class, () -> matchService.joinGame(auth, 3L));

        verify(gamePlayerService, never()).save(any());
    }

    @Test
    void createGame_inOccupiedLocation_throwsAndDoesNotSave () {
        doThrow(new GameAlreadyExistsException("loc")).when(gameValidation).notExists("loc");

        assertThrows(GameAlreadyExistsException.class,
          () -> matchService.createGame(auth, NationType.WATER, "loc"));

        verify(gameService, never()).save(any());
        verify(gamePlayerService, never()).save(any());
    }
}
