package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.dto.PlayerDTO;
import dev.racnar.shipbender.player.dto.PlayerStatsDTO;
import dev.racnar.shipbender.player.dto.ScoreStatsDTO;
import dev.racnar.shipbender.player.dto.SignInPlayerDTO;
import dev.racnar.shipbender.shared.error.conflict.EmailAlreadyUseException;
import dev.racnar.shipbender.shared.error.not_found.PlayerNotFoundException;
import dev.racnar.shipbender.shared.error.unauthorized.PlayerNotLoginException;
import dev.racnar.shipbender.player.domain.Player;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    PlayerRepository playerRepository;
    ModelMapper      mapper;

    public PlayerService ( PlayerRepository playerRepository, ModelMapper mapper ) {
        this.playerRepository = playerRepository;
        this.mapper           = mapper;
    }

    public Player getPlayer ( String email ) {
        return playerRepository.findByEmail(email)
          .orElseThrow(() -> new PlayerNotFoundException(email));
    }

    public PlayerStatsDTO getAnyPlayer ( Authentication authentication ) {
        Authentication auth = SecurityContextHolder.getContext()
          .getAuthentication();
        if (isGuest(auth)) return new PlayerStatsDTO("Guess");
        Player    player    = getPlayer(auth.getName());
        PlayerDTO playerDTO = mapper.map(player, PlayerDTO.class);
        return new PlayerStatsDTO(playerDTO, getStatsByPlayer(player.getScores()));
    }

    public Player getPlayerAuthenticated ( Authentication authentication ) {
        Authentication auth = SecurityContextHolder.getContext()
          .getAuthentication();
        if (isGuest(auth)) throw new PlayerNotLoginException();
        else return getPlayer(auth.getName());
    }

    public List<PlayerDTO> getPlayers () {
        List<Player> players = playerRepository.findAll();
        return players.stream()
          .map(p -> mapper.map(p, PlayerDTO.class))
          .collect(Collectors.toList());
    }

    public List<PlayerStatsDTO> getPlayersScore () {
        return playerRepository.rankedPlayer()
          .stream()
          .map(p -> new PlayerStatsDTO(
              mapper.map(p, PlayerDTO.class),
              getStatsByPlayer(p.getScores())
            )
          )
          .collect(Collectors.toList());
    }

    public PlayerDTO save ( Player player ) {
        return mapper.map(playerRepository.save(player), PlayerDTO.class);
    }

    public void validated ( Player player ) {

    }

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
