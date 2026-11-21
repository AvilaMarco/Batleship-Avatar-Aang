package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.dto.CreateGameRequest;
import dev.racnar.shipbender.game.dto.GameSummary;
import dev.racnar.shipbender.game.dto.GameView;
import dev.racnar.shipbender.game.dto.PlaceShipsRequest;
import dev.racnar.shipbender.game.dto.SalvoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController ( GameService service ) {
        this.service = service;
    }

    @GetMapping
    public List<GameSummary> list () {
        return service.list();
    }

    @PostMapping
    public ResponseEntity<GameView> create ( Principal principal, @Valid @RequestBody CreateGameRequest request ) {
        GameView view = service.create(principal.getName(), request.nation(), request.location());
        return ResponseEntity.created(URI.create("/api/games/" + view.id())).body(view);
    }

    @PostMapping("/{id}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public GameView join ( Principal principal, @PathVariable long id ) {
        return service.join(principal.getName(), id);
    }

    @GetMapping("/{id}")
    public GameView get ( Principal principal, @PathVariable long id ) {
        return service.get(principal.getName(), id);
    }

    @PostMapping("/{id}/ships")
    @ResponseStatus(HttpStatus.CREATED)
    public GameView placeShips ( Principal principal, @PathVariable long id, @Valid @RequestBody PlaceShipsRequest request ) {
        return service.placeShips(principal.getName(), id, request.toFleet());
    }

    @PostMapping("/{id}/salvos")
    @ResponseStatus(HttpStatus.CREATED)
    public GameView fire ( Principal principal, @PathVariable long id, @Valid @RequestBody SalvoRequest request ) {
        return service.fire(principal.getName(), id, request.cells());
    }
}
