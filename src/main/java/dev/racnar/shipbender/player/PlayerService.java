package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.player.domain.Rol;
import dev.racnar.shipbender.player.dto.*;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.springframework.dao.DataIntegrityViolationException;
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
          .orElseThrow(() -> new ShipbenderException(ErrorCode.UNAUTHENTICATED, "El jugador del token ya no existe"));
    }

    public PlayerProfile register ( RegisterRequest request ) {
        if (playerRepository.existsByEmail(request.email()))
            throw new ShipbenderException(ErrorCode.EMAIL_IN_USE, "El email " + request.email() + " ya esta registrado");
        Player player = new Player(request.name(), request.email(), encoder.encode(request.password()), null);
        player.addRol(Rol.PLAYER);
        try {
            return PlayerProfile.from(playerRepository.saveAndFlush(player));
        } catch (DataIntegrityViolationException e) { // registro concurrente: gana la restricción unique
            throw new ShipbenderException(ErrorCode.EMAIL_IN_USE, "El email " + request.email() + " ya esta registrado");
        }
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
}
