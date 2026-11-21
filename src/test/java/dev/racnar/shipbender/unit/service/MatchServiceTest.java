package dev.racnar.shipbender.unit.service;

import dev.racnar.shipbender.game.GamePlayerService;
import dev.racnar.shipbender.game.GameService;
import dev.racnar.shipbender.game.GameValidation;
import dev.racnar.shipbender.player.PlayerService;

import dev.racnar.shipbender.game.dto.GameMapDTO;
import dev.racnar.shipbender.game.dto.GameCreatedDTO;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.shared.error.conflict.GameAlreadyExistsException;
import dev.racnar.shipbender.shared.error.conflict.GameIsFullException;
import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.domain.GamePlayer;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.game.MatchService;
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
    PlayerService playerService;
    @Mock
    GameService gameService;
    @Mock
    GamePlayerService gamePlayerService;
    @Mock
    GameValidation gameValidation;

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
