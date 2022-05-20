package com.codeblockacademy.shipbender.service;

import com.codeblockacademy.shipbender.dto.PlayerDTO;
import com.codeblockacademy.shipbender.dto.PlayerScoreDTO;
import com.codeblockacademy.shipbender.dto.ScoreStatsDTO;
import com.codeblockacademy.shipbender.dto.request.SignInPlayerDTO;
import com.codeblockacademy.shipbender.exception.conflict.EmailAlreadyUseException;
import com.codeblockacademy.shipbender.exception.not_found.PlayerNotFoundException;
import com.codeblockacademy.shipbender.exception.unauthorized.PlayerNotLoginException;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.repository.PlayerRepository;
import com.codeblockacademy.shipbender.service.intereface.IPlayerService;
import com.codeblockacademy.shipbender.service.intereface.ISessionService;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService implements IPlayerService {

    PlayerRepository playerRepository;
    ModelMapper      mapper;
    ISessionService  sessionService;

    public PlayerService ( PlayerRepository playerRepository, ModelMapper mapper, ISessionService sessionService ) {
        this.playerRepository = playerRepository;
        this.mapper           = mapper;
        this.sessionService   = sessionService;
    }

    @Override
    public Player getPlayer ( String email ) {
        return playerRepository.findByEmail(email)
          .orElseThrow(() -> new PlayerNotFoundException(email));
    }

    @Override
    public PlayerScoreDTO getAnyPlayer ( Authentication authentication ) {
        if (isGuest(authentication)) return new PlayerScoreDTO("Guess");
        Player    player    = getPlayer(authentication.getName());
        PlayerDTO playerDTO = mapper.map(player, PlayerDTO.class);
        return new PlayerScoreDTO(playerDTO, getStatsByPlayer(player.getScores()));
    }

    @Override
    public Player getPlayerAuthenticated ( Authentication authentication ) {
        if (isGuest(authentication)) throw new PlayerNotLoginException();
        else return getPlayer(authentication.getName());
    }

    @Override
    public List<PlayerDTO> getPlayers () {
        List<Player> players = playerRepository.findAll();
        return players.stream()
          .map(p -> mapper.map(p, PlayerDTO.class))
          .collect(Collectors.toList());
    }

    @Override
    public List<PlayerScoreDTO> getPlayersScore () {
        return playerRepository.rankedPlayer()
          .stream()
          .map(p -> new PlayerScoreDTO(
              mapper.map(p, PlayerDTO.class),
              getStatsByPlayer(p.getScores())
            )
          )
          .collect(Collectors.toList());
    }

    @Override
    public PlayerDTO save ( Player player ) {
        return mapper.map(playerRepository.save(player), PlayerDTO.class);
    }

    @Override
    public void validated ( Player player ) {

    }

    @Override
    public void isNotRegister ( SignInPlayerDTO player ) {
        String email = player.getEmail();
        if (playerRepository.existsPlayer(email)
          .isPresent())
            throw new EmailAlreadyUseException(email);
    }

    private ScoreStatsDTO getStatsByPlayer ( List<Integer> scores ) {
        ScoreStatsDTO stats = new ScoreStatsDTO(0, 0, 0, 0, 0D);
        if (scores.isEmpty()) return stats;
        scores.forEach(score -> {
            stats.plusScore(score);
            if (score == 3) stats.plusWon();
            else if (score == 1) stats.plusTied();
            else if (score == 0) stats.plusLost();
        });
        Double winRate = (double) ( stats.getWon() / scores.size() ) * 100D;
        stats.setWinRate(winRate);
        return stats;
    }

    // TODO: New service for save this methods
    private boolean isGuest ( Authentication authentication ) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
}
