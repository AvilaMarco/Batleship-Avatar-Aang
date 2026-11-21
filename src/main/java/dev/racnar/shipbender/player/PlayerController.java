package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.dto.PlayerProfile;
import dev.racnar.shipbender.player.dto.RankingEntry;
import dev.racnar.shipbender.player.dto.RegisterRequest;
import dev.racnar.shipbender.player.dto.UpdatePlayerRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController ( PlayerService playerService ) {
        this.playerService = playerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerProfile register ( @Valid @RequestBody RegisterRequest request ) {
        return playerService.register(request);
    }

    @GetMapping("/me")
    public PlayerProfile me ( Principal principal ) {
        return playerService.me(principal.getName());
    }

    @PatchMapping("/me")
    public PlayerProfile updateNation ( Principal principal, @Valid @RequestBody UpdatePlayerRequest request ) {
        return playerService.updateNation(principal.getName(), request.nation());
    }

    @GetMapping("/ranking")
    public List<RankingEntry> ranking () {
        return playerService.ranking();
    }
}
