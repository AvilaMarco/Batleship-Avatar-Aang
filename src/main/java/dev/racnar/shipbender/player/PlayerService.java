package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.player.domain.PlayerStats;
import dev.racnar.shipbender.player.domain.Rol;
import dev.racnar.shipbender.player.dto.*;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import dev.racnar.shipbender.shared.error.not_found.PlayerNotFoundException;
import dev.racnar.shipbender.shared.error.unauthorized.PlayerNotLoginException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder  encoder;

    public PlayerService ( PlayerRepository playerRepository, PasswordEncoder encoder ) {
        this.playerRepository = playerRepository;
        this.encoder          = encoder;
    }

    public Player getPlayer ( String email ) {
        return playerRepository.findByEmail(email)
          .orElseThrow(() -> new PlayerNotFoundException(email));
    }

    public PlayerProfile register ( RegisterRequest request ) {
        if (playerRepository.existsPlayer(request.email()).isPresent())
            throw new ShipbenderException(ErrorCode.EMAIL_IN_USE, "El email " + request.email() + " ya esta registrado");
        Player player = new Player(request.name(), request.email(), encoder.encode(request.password()), null);
        player.addRol(Rol.PLAYER);
        return PlayerProfile.from(playerRepository.save(player));
    }

    public PlayerProfile me ( String email ) {
        return PlayerProfile.from(getPlayer(email));
    }

    public PlayerProfile updateNation ( String email, NationType nation ) {
        Player player = getPlayer(email);
        player.setNation(nation);
        return PlayerProfile.from(playerRepository.save(player));
    }

    public List<RankingEntry> ranking () {
        return playerRepository.rankedPlayer().stream()
          .map(RankingEntry::from)
          .sorted(Comparator.comparingInt((RankingEntry e) -> e.stats().score()).reversed())
          .toList();
    }

    // v1 (hasta la 407): los servicios de game aun pasan Authentication
    public Player getPlayerAuthenticated ( Authentication authentication ) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            throw new PlayerNotLoginException();
        return getPlayer(authentication.getName());
    }

    // v1 (hasta la 407), lo usa SalvoService.getInfoGames
    public PlayerStatsDTO getAnyPlayer ( Authentication authentication ) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
            return new PlayerStatsDTO("Guess");
        return toV1(getPlayer(authentication.getName()));
    }

    // v1 (hasta la 407)
    public List<PlayerStatsDTO> getPlayersScore () {
        return playerRepository.rankedPlayer().stream().map(this::toV1).toList();
    }

    private PlayerStatsDTO toV1 ( Player p ) {
        PlayerStats s = PlayerStats.of(p.getScores());
        return new PlayerStatsDTO(new PlayerDTO(p.getId(), p.getName(), p.getEmail(), p.getNation()),
          new ScoreStatsDTO(s.score(), s.won(), s.tied(), s.lost(), s.winRate()));
    }
}
