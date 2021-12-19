package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.exception.not_found.PlayerNotFoundException;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import com.codeoftheweb.salvo.service.intereface.IPlayerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService implements IPlayerService {

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    ModelMapper mapper;

    @Override
    public PlayerDTO getPlayer(String email) {
        Player player = playerRepository.findByEmail(email).orElseThrow(() -> new PlayerNotFoundException(email));
        return mapper.map(player, PlayerDTO.class);
    }

    @Override
    public List<PlayerDTO> getPlayers() {
        List<Player> players = playerRepository.findAll();
        return players.stream()
                .map( p -> mapper.map(p, PlayerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public PlayerDTO save(Player player) {
        return mapper.map(playerRepository.save(player), PlayerDTO.class);
    }
}
