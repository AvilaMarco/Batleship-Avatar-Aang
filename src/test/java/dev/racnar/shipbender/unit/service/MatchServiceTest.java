package dev.racnar.shipbender.unit.service;

import dev.racnar.shipbender.dto.GameMapDTO;
import dev.racnar.shipbender.dto.response.GameCreatedDTO;
import dev.racnar.shipbender.enums.NationType;
import dev.racnar.shipbender.exception.conflict.GameAlreadyExistsException;
import dev.racnar.shipbender.exception.conflict.GameIsFullException;
import dev.racnar.shipbender.models.Game;
import dev.racnar.shipbender.models.GamePlayer;
import dev.racnar.shipbender.models.Player;
import dev.racnar.shipbender.service.MatchService;
import dev.racnar.shipbender.service.intereface.IGamePlayerService;
import dev.racnar.shipbender.service.intereface.IGameService;
import dev.racnar.shipbender.service.intereface.IPlayerService;
import dev.racnar.shipbender.service.validations.IGameValidation;
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
