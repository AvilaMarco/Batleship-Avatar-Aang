package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.dto.GameDTO;
import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.exception.not_found.GameNotFoundException;
import com.codeoftheweb.salvo.exception.not_found.NotFoundException;
import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.service.intereface.IGameService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService implements IGameService {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    ModelMapper mapper;

    // TODO: Add optional response into game repository with exception
    @Override
    public GameDTO getGame(String direction) {
        Game game = gameRepository.findByDirection(direction);
        return mapper.map(game, GameDTO.class);
    }

    @Override
    public GameDTO getGame(Long id) {
        Game game = gameRepository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
        return mapper.map(game, GameDTO.class);
    }

    @Override
    public List<GameDTO> getGames() {
        List<Game> games =gameRepository.findAll();
        return games.stream()
                .map( g -> mapper.map(g, GameDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public GameDTO save(Game game) {
        return mapper.map(gameRepository.save(game), GameDTO.class);
    }
}
