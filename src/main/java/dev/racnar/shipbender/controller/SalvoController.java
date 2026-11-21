package dev.racnar.shipbender.controller;

import dev.racnar.shipbender.dto.response.MenuViewDTO;
import dev.racnar.shipbender.service.intereface.ISalvoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class SalvoController {

    @Autowired
    ISalvoService salvoService;

    // listar info juegos
    @GetMapping
    public MenuViewDTO getGamesAndPlayers ( Authentication authentication ) {
        return salvoService.getInfoGames(authentication);
    }

}
