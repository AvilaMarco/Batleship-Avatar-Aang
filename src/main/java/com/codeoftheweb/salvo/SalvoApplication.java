package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.enums.TypeShips;
import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {

		SpringApplication.run(SalvoApplication.class, args);
	}

    @Bean
    public CommandLineRunner initData(PlayerRepository PlayerRepository, GameRepository GameRepository,
          GamePlayerRepository GamePlayerRepository, ShipRepository ShipRepository, ScoreRepository ScoreRepository) {
        return (args) -> {
            //variables pruebas
            Player jack = new Player("Jack", "Bauer","j.bauer@ctu.gov");
            Player Chloe = new Player("Chloe", "O'Brian","c.obrian@ctu.gov");
            Player Kim = new Player("Kim", "Bauer","kim_bauer@gmail.com");
            Player Tony = new Player("Tony", "Almeida","t.almeida@ctu.gov");

            Game one = new Game(0);
            Game two = new Game(1);
            Game three = new Game(2);

            //ships position
            List<String> position01 = Arrays.asList("A1","A2","A3","A4","A5");
            List<String> position02 = Arrays.asList("B1","B2","B3","B4");
            List<String> position03 = Arrays.asList("C1","D1","E1");
            List<String> position04 = Arrays.asList("F3", "G3", "H3");
            List<String> position05 = Arrays.asList("B5", "B6");

            Ship carrier = new Ship(TypeShips.carrier,position01);
            Ship battleship = new Ship(TypeShips.battleship,position02);
            Ship submarine = new Ship(TypeShips.submarine,position03);
            Ship destroyer = new Ship(TypeShips.destroyer,position04);
            Ship patrolBoat = new Ship(TypeShips.patrol_boat,position05);
            Ship carrier2 = new Ship(TypeShips.carrier,position01);
            Ship battleship2 = new Ship(TypeShips.battleship,position02);
            Ship submarine2 = new Ship(TypeShips.submarine,position03);
            Ship destroyer2 = new Ship(TypeShips.destroyer,position04);
            Ship patrolBoat2 = new Ship(TypeShips.patrol_boat,position05);

            //salvo position
            List<String> sPosition01 = Arrays.asList("B5","C5","F1");
            List<String> sPosition02 = Arrays.asList("B4","B5","B6");
            List<String> sPosition03 = Arrays.asList("F2","D5");
            List<String> sPosition04 = Arrays.asList("E1", "H3", "A2");
            List<String> sPosition05 = Arrays.asList("A2", "A4","G6");
            List<String> sPosition06 = Arrays.asList("B5", "D5","C7");

            Salvo salvo1 = new Salvo(sPosition01,1);
            Salvo salvo2 = new Salvo(sPosition02,1);
            Salvo salvo3 = new Salvo(sPosition03,2);
            Salvo salvo4 = new Salvo(sPosition04,2);

            GamePlayer GamePlayer1 = new GamePlayer(jack,one);
                //ships
                GamePlayer1.addShip(carrier);
                GamePlayer1.addShip(battleship);
                GamePlayer1.addShip(submarine);
                GamePlayer1.addShip(destroyer);
                GamePlayer1.addShip(submarine);
                GamePlayer1.addShip(patrolBoat);

                //salvo
                GamePlayer1.addSalvo(salvo1);
                GamePlayer1.addSalvo(salvo3);
            GamePlayer GamePlayer2 = new GamePlayer(Chloe,one);
                //ships
                GamePlayer2.addShip(carrier2);
                GamePlayer2.addShip(battleship2);
                GamePlayer2.addShip(submarine2);
                GamePlayer2.addShip(destroyer2);
                GamePlayer2.addShip(submarine2);
                GamePlayer2.addShip(patrolBoat2);

                //salvoes
                GamePlayer2.addSalvo(salvo2);
                GamePlayer2.addSalvo(salvo4);
            GamePlayer GamePlayer3 = new GamePlayer(Kim,two);
            GamePlayer GamePlayer4 = new GamePlayer(jack,two);
            GamePlayer GamePlayer5 = new GamePlayer(jack,three);
            GamePlayer GamePlayer6 = new GamePlayer(Tony,three);

            //scores games
            Score puntaja1 = new Score(jack,one,3, LocalDateTime.now());
            Score puntaja2 = new Score(Chloe,one,0, LocalDateTime.now());
            Score puntaja3 = new Score(jack,two,1, LocalDateTime.now());
            Score puntaja4 = new Score(Tony,two,1, LocalDateTime.now());
            Score puntaja5 = new Score(Tony,three,3, LocalDateTime.now());
            Score puntaja6 = new Score(Chloe,three,0, LocalDateTime.now());
            // save players in the PlayerRepository
            PlayerRepository.save(jack);
            PlayerRepository.save(Chloe);
            PlayerRepository.save(Kim);
            PlayerRepository.save(Tony);

            // save players in the GameRepository
            GameRepository.save(one);
            GameRepository.save(two);
            GameRepository.save(three);

            // save players in the GamePlayerRepository
            GamePlayerRepository.save(GamePlayer1);
            GamePlayerRepository.save(GamePlayer2);
            GamePlayerRepository.save(GamePlayer3);
            GamePlayerRepository.save(GamePlayer4);
            GamePlayerRepository.save(GamePlayer5);
            GamePlayerRepository.save(GamePlayer6);

            //guardo scores
            ScoreRepository.save(puntaja1);
            ScoreRepository.save(puntaja2);
            ScoreRepository.save(puntaja3);
            ScoreRepository.save(puntaja4);
            ScoreRepository.save(puntaja5);
            ScoreRepository.save(puntaja6);

        };
    }
}
