package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.dto.GameCreatedDTO;
import dev.racnar.shipbender.game.dto.GameDataDTO;
import dev.racnar.shipbender.game.dto.GameMatchDTO;
import dev.racnar.shipbender.shared.NationType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    MatchService    matchService;
    GameplayService gameplayService;

    public MatchController ( MatchService matchService, GameplayService gameplayService ) {
        this.matchService    = matchService;
        this.gameplayService = gameplayService;
    }

    @GetMapping("/{game_id}/status")
    public GameDataDTO statusGame ( @PathVariable Long game_id, Authentication authentication ) {
        return matchService.statusGame(authentication, game_id);
    }

    @GetMapping("/{game_id}")
    public GameMatchDTO dataGame ( @PathVariable Long game_id, Authentication authentication ) {
        return matchService.getGame(authentication, game_id);
    }

    // crear juegos
    @PostMapping(path = "/games/{nation}/{location}")
    public ResponseEntity<GameCreatedDTO> createGame ( Authentication authentication, @PathVariable NationType nation, @PathVariable String location ) {
        return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(matchService.createGame(authentication, nation, location));
    }

    // unirse a juegos
    @PostMapping(path = "/games/{id}")
    public GameCreatedDTO joinGame ( Authentication authentication, @PathVariable Long id ) {
        return matchService.joinGame(authentication, id);
    }
}
