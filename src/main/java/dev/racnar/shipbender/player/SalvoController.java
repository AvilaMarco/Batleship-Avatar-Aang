package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.dto.MenuViewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class SalvoController {

    @Autowired
    SalvoService salvoService;

    // listar info juegos
    @GetMapping
    public MenuViewDTO getGamesAndPlayers ( Authentication authentication ) {
        return salvoService.getInfoGames(authentication);
    }

}
