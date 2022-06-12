package com.codeblockacademy.shipbender.unit.service;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.GamePlayerDTO;
import com.codeblockacademy.shipbender.dto.response.GameCreatedDTO;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.service.GamePlayerService;
import com.codeblockacademy.shipbender.service.GameService;
import com.codeblockacademy.shipbender.service.PlayerService;
import com.codeblockacademy.shipbender.service.main.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class MatchServiceTest {

    @Mock
    PlayerService     playerService;
    @Mock
    GameService       gameService;
    @Mock
    GamePlayerService gamePlayerService;

    @Spy
    ModelMapper mapper;

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

        when(gameService.getGame(gameId)).thenReturn(gameMatchDTO);


        doNothing()
          .when(gameService)
          .gameIsNotFull(any());


/*        lenient().doNothing()
          .when(gameService)
          .gameNotContainsThePlayer(any(), eq(anyLong()));*/

        when(gamePlayerService.save(any())).thenReturn(any(GamePlayerDTO.class));

        GameCreatedDTO result = matchService.joinGame(authMock, gameId);
        log.debug("mensaje: {}", result);
    }
}
