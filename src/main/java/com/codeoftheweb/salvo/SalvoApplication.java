package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.enums.TypeShips;
import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository PlayerRepository, GameRepository GameRepository,
          GamePlayerRepository GamePlayerRepository, ShipRepository ShipRepository, ScoreRepository ScoreRepository) {
        return (args) -> {

            //variables pruebas
            Player jack = new Player("Jack", "Bauer","j.bauer@ctu.gov","j.bauer@ctu.gov",passwordEncoder().encode("24"),"tierra");
            Player Chloe = new Player("Chloe", "O'Brian","c.obrian@ctu.gov","c.obrian@ctu.gov",passwordEncoder().encode("42"),"agua");
            Player Kim = new Player("Kim", "Bauer","kim_bauer@gmail.com","kim_bauer@gmail.com",passwordEncoder().encode("kb"),"fuego");
            Player Tony = new Player("Tony", "Almeida","t.almeida@ctu.gov","t.almeida@ctu.gov",passwordEncoder().encode("mole"),"viento");

            Game one = new Game(0,"tierra","00");
            Game two = new Game(1,"agua","00");
            Game three = new Game(2,"fuego","00");

            //ships position
            List<String> position01 = Arrays.asList("A1","A2","A3","A4","A5");
            List<String> position02 = Arrays.asList("B1","B2","B3","B4");
            List<String> position03 = Arrays.asList("C1","D1","E1");
            List<String> position04 = Arrays.asList("F3", "G3", "H3");
            List<String> position05 = Arrays.asList("B5", "B6");
            List<String> position06 = Arrays.asList("J1","J2","J3","J4","J5");
            List<String> position07 = Arrays.asList("A1","B1","C1","D1");
            List<String> position08 = Arrays.asList("C4","D4","E4");
            List<String> position09 = Arrays.asList("F1", "F2", "F3");
            List<String> position10 = Arrays.asList("C5", "C6");

            Ship carrier = new Ship(TypeShips.CARRIER,position01);
            Ship battleship = new Ship(TypeShips.BATTLESHIP,position02);
            Ship submarine = new Ship(TypeShips.SUBMARINE,position03);
            Ship destroyer = new Ship(TypeShips.DESTROYER,position04);
            Ship patrolBoat = new Ship(TypeShips.PATROL_BOAT,position05);
            Ship carrier2 = new Ship(TypeShips.CARRIER,position06);
            Ship battleship2 = new Ship(TypeShips.BATTLESHIP,position07);
            Ship submarine2 = new Ship(TypeShips.SUBMARINE,position08);
            Ship destroyer2 = new Ship(TypeShips.DESTROYER,position09);
            Ship patrolBoat2 = new Ship(TypeShips.PATROL_BOAT,position10);

            //salvo position
            List<String> sPosition01 = Arrays.asList("B5","C5","F1","J10","A1");
            List<String> sPosition02 = Arrays.asList("B4","B5","B6","F5","F6");
            List<String> sPosition03 = Arrays.asList("F2","D5","G3","G2","G1");
            List<String> sPosition04 = Arrays.asList("E1", "H3", "A2","C2","C3");
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
            Score puntaja3 = new Score(Kim,two,1, LocalDateTime.now());
            Score puntaja4 = new Score(jack,two,1, LocalDateTime.now());
            Score puntaja5 = new Score(jack,three,3, LocalDateTime.now());
            Score puntaja6 = new Score(Tony,three,0, LocalDateTime.now());
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

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private com.codeoftheweb.salvo.repository.PlayerRepository PlayerRepository;

    //cuando el usuario llega se codifica la contraseña y compara la String resultante con la
    //contraseña codificada que exite
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName-> {
            Player player = PlayerRepository.findByCorreo(inputName);
            if (player != null && !player.getCorreo().equals("racnar1")) {
                return new User(player.getCorreo(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else if(player != null && player.getCorreo().equals("racnar1")){
                return new User(player.getCorreo(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("ADMIN"));
            }else{
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }

}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //indico las direcciones a las quepuedeo acceder
                .antMatchers("/rest/**").hasAuthority("ADMIN")
                .antMatchers("/api/gp/**").hasAuthority("USER");


        http.formLogin()
                .usernameParameter("user-name")
                .passwordParameter("user-password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

    }

