package com.codeoftheweb.salvo.config;

import com.codeoftheweb.salvo.enums.NationType;
import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class SpringConfig {

    @Bean
    public ModelMapper getModelMapper() {
        /*        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                *//*.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)*//*
                .setSourceNamingConvention(NamingConventions.JAVABEANS_MUTATOR);*/
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

   @Bean
    public CommandLineRunner initData(PlayerRepository PlayerRepository, GameRepository GameRepository,
                                      GamePlayerRepository GamePlayerRepository) {
        return (args) -> {

            //variables pruebas  WATER, AIR, FIRE, EARTH
            Player jack = new Player("Jack Bauer", "j.bauer@ctu.gov", passwordEncoder().encode("24"), NationType.AIR);
            Player Chloe = new Player("Chloe O'Brian", "c.obrian@ctu.gov", passwordEncoder().encode("42"), NationType.WATER);
            Player Kim = new Player("Kim Bauer", "kim_bauer@gmail.com", passwordEncoder().encode("kb"), NationType.FIRE);
            Player Tony = new Player("Tony Almeida", "t.almeida@ctu.gov", passwordEncoder().encode("mole"), NationType.AIR);
            Player marco = new Player("Marco Avila", "marco@aaa.com", passwordEncoder().encode("123"), NationType.EARTH);

            Game one = new Game(NationType.EARTH, "E1");
            Game two = new Game(NationType.WATER, "W1");
            Game three = new Game(NationType.FIRE, "F1");
            Game four = new Game(NationType.AIR, "A1");

            //ships position
/*            List<String> position01 = Arrays.asList("A1", "A2", "A3", "A4", "A5");
            List<String> position02 = Arrays.asList("B1", "B2", "B3", "B4");
            List<String> position03 = Arrays.asList("C1", "D1", "E1");
            List<String> position04 = Arrays.asList("F3", "G3", "H3");
            List<String> position05 = Arrays.asList("B5", "B6");
            List<String> position06 = Arrays.asList("J1", "J2", "J3", "J4", "J5");
            List<String> position07 = Arrays.asList("A1", "B1", "C1", "D1");
            List<String> position08 = Arrays.asList("C4", "D4", "E4");
            List<String> position09 = Arrays.asList("F1", "F2", "F3");
            List<String> position10 = Arrays.asList("C5", "C6");

            Ship carrier = new Ship(TypeShips.CARRIER, position01);
            Ship battleship = new Ship(TypeShips.BATTLESHIP, position02);
            Ship submarine = new Ship(TypeShips.SUBMARINE, position03);
            Ship destroyer = new Ship(TypeShips.DESTROYER, position04);
            Ship patrolBoat = new Ship(TypeShips.PATROL_BOAT, position05);
            Ship carrier2 = new Ship(TypeShips.CARRIER, position06);
            Ship battleship2 = new Ship(TypeShips.BATTLESHIP, position07);
            Ship submarine2 = new Ship(TypeShips.SUBMARINE, position08);
            Ship destroyer2 = new Ship(TypeShips.DESTROYER, position09);
            Ship patrolBoat2 = new Ship(TypeShips.PATROL_BOAT, position10);*/

            //salvo position
/*            List<String> sPosition01 = Arrays.asList("B5", "C5", "F1", "J10", "A1");
            List<String> sPosition02 = Arrays.asList("B4", "B5", "B6", "F5", "F6");
            List<String> sPosition03 = Arrays.asList("F2", "D5", "G3", "G2", "G1");
            List<String> sPosition04 = Arrays.asList("E1", "H3", "A2", "C2", "C3");

            Salvo salvo1 = new Salvo(sPosition01, 1);
            Salvo salvo2 = new Salvo(sPosition02, 1);
            Salvo salvo3 = new Salvo(sPosition03, 2);
            Salvo salvo4 = new Salvo(sPosition04, 2);*/

            GamePlayer GamePlayer1 = new GamePlayer(jack, one);
            GamePlayer1.setScore(3);
            //ships
/*            GamePlayer1.addShip(carrier);
            GamePlayer1.addShip(battleship);
            GamePlayer1.addShip(submarine);
            GamePlayer1.addShip(destroyer);
            GamePlayer1.addShip(submarine);
            GamePlayer1.addShip(patrolBoat);*/

            //salvo
/*            GamePlayer1.addSalvo(salvo1);
            GamePlayer1.addSalvo(salvo3);*/

            GamePlayer GamePlayer2 = new GamePlayer(Chloe, one);
            GamePlayer2.setScore(0);
            one.setFinishDate(LocalDateTime.now());
            //ships
/*            GamePlayer2.addShip(carrier2);
            GamePlayer2.addShip(battleship2);
            GamePlayer2.addShip(submarine2);
            GamePlayer2.addShip(destroyer2);
            GamePlayer2.addShip(submarine2);
            GamePlayer2.addShip(patrolBoat2);*/

            //salvoes
/*            GamePlayer2.addSalvo(salvo2);
            GamePlayer2.addSalvo(salvo4);*/

            GamePlayer GamePlayer3 = new GamePlayer(Kim, two);
            GamePlayer GamePlayer4 = new GamePlayer(jack, two);
            GamePlayer GamePlayer5 = new GamePlayer(marco, three);
            GamePlayer GamePlayer6 = new GamePlayer(Chloe, four);

            // save players in the PlayerRepository
            List<Player> players = List.of(
                    jack, Chloe, Kim, Tony, marco
            );
            PlayerRepository.saveAll(players);

            // save players in the GameRepository
            GameRepository.save(one);
            GameRepository.save(two);
            GameRepository.save(three);
            GameRepository.save(four);

            // save players in the GamePlayerRepository
            List<GamePlayer> gps = List.of(
                    GamePlayer1, GamePlayer2, GamePlayer3, GamePlayer4, GamePlayer5, GamePlayer6
            );
            GamePlayerRepository.saveAll(gps);

        };
    }
}
