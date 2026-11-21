package dev.racnar.shipbender.config;

import dev.racnar.shipbender.game.GameRepository;
import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.player.PlayerRepository;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.player.domain.Rol;
import dev.racnar.shipbender.shared.NationType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final PlayerRepository players;
    private final GameRepository   games;
    private final PasswordEncoder  encoder;

    public DevDataSeeder ( PlayerRepository players, GameRepository games, PasswordEncoder encoder ) {
        this.players = players;
        this.games   = games;
        this.encoder = encoder;
    }

    @Override
    public void run ( String... args ) {
        Player jack  = new Player("Jack Bauer", "j.bauer@ctu.gov", encoder.encode("24"), NationType.AIR);
        Player chloe = new Player("Chloe O'Brian", "c.obrian@ctu.gov", encoder.encode("42"), NationType.WATER);
        Player kim   = new Player("Kim Bauer", "kim_bauer@gmail.com", encoder.encode("kb"), NationType.FIRE);
        Player tony  = new Player("Tony Almeida", "t.almeida@ctu.gov", encoder.encode("mole"), NationType.AIR);
        Player marco = new Player("Marco Avila", "marco@aaa.com", encoder.encode("123"), NationType.EARTH);
        marco.setRoles(List.of(Rol.PLAYER));
        Player admin = new Player("Marco Avila", "marco@admin.com", encoder.encode("123"), NationType.EARTH);
        admin.setRoles(List.of(Rol.ADMIN, Rol.PLAYER, Rol.GUEST));
        players.saveAll(List.of(jack, chloe, kim, tony, marco, admin)); // join() necesita ids

        Game one   = new Game(NationType.EARTH, "E1");
        Game two   = new Game(NationType.WATER, "W1");
        Game three = new Game(NationType.FIRE, "F1");
        Game four  = new Game(NationType.AIR, "A1");

        one.join(jack).setScore(3);
        one.join(admin).setScore(0);
        one.finishDate();
        two.join(kim);
        two.join(jack);
        three.join(admin);
        three.join(marco);
        four.join(chloe);

        games.saveAll(List.of(one, two, three, four));
    }
}
