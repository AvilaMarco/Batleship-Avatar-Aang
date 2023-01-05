package com.codeblockacademy.shipbender.unit.service;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.response.GameCreatedDTO;
import com.codeblockacademy.shipbender.models.Game;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.service.MatchService;
import com.codeblockacademy.shipbender.service.intereface.IGamePlayerService;
import com.codeblockacademy.shipbender.service.intereface.IGameService;
import com.codeblockacademy.shipbender.service.intereface.IPlayerService;
import com.codeblockacademy.shipbender.service.validations.IGameValidation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class MatchServiceTest {

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

    @Test
    void joinGameTest () {
        Long   gameId     = 1L;
        Long   playerId   = 1L;
        Player playerMock = new Player();
        playerMock.setId(playerId);
        GameMapDTO gameMatchDTO = new GameMapDTO();
        gameMatchDTO.setId(gameId);
        Authentication authMock = any(Authentication.class);


        lenient().when(playerService.getPlayerAuthenticated(authMock))
          .thenReturn(playerMock);

        when(gameService.getGame(gameId)).thenReturn(new Game());


//        doNothing()
//          .when(gameService)
//          .gameIsNotFull(any());


/*        lenient().doNothing()
          .when(gameService)
          .gameNotContainsThePlayer(any(), eq(anyLong()));*/

        when(gamePlayerService.save(any())).thenReturn(any(GamePlayer.class));

        GameCreatedDTO result = matchService.joinGame(authMock, gameId);
        log.debug("mensaje: {}", result);
    }
}
