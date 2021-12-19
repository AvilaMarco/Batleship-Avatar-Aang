package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.service.PlayerService;

import java.util.List;

public interface IPlayerService {

    PlayerDTO getPlayer(String email);
    List<PlayerDTO> getPlayers();
    PlayerDTO save(Player player);
}
