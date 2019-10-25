package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;

import com.codeoftheweb.salvo.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.awt.image.ImageProducer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*Anotación que le dice a Spring que vamos a hacer un controller que cumple con los requisitos REST.
Es decir, que vamos a utilizar entre otras cosas los métodos GET, POST, PUT, etc.
*/
@RestController
//Anotación que declara como va a ser la estructura de nuestros end-point. Todos van a comenzar con '/api'
@RequestMapping("/api")
public class SalvoController {
        //Anotación que le dice a Spring que traiga toda la información y métodos del repositorio o clase a la que
        //hacemos referencia, podemos instanciar un repositorio
        @Autowired
        private com.codeoftheweb.salvo.repository.GameRepository gameRepository;

        @Autowired
        private com.codeoftheweb.salvo.repository.PlayerRepository PlayerRepository;

        @Autowired
        private com.codeoftheweb.salvo.repository.GamePlayerRepository gamePlayerRepository;

        @Autowired
        PasswordEncoder passwordEncoder;

//        codificar contraseña
//        passwordEncoder.encode(password)

        private boolean isGuest(Authentication authentication) {
            return authentication == null || authentication instanceof AnonymousAuthenticationToken;
        }
        //Creamos nuestro primer end-point con la dirección '/api/drivers'
        @RequestMapping("/games")
        //Definimos un método para administrar la información que brinda nuestro end-point
        //Como todos los métodos, definimos método de acceso, tipo de dato que retorna, nombre y parámetros.
        //El método ingresa al repositorio de los jugadores y trae toda la información
        //Recorremos esa colección de Drivers y llamamos por cada una a su DTO para almacenar su información
        //en una lista para enviarla a la front.
        public Map<String, Object> getGamesAndPlayers(Authentication authentication){
            Map<String,Object> mapa = new LinkedHashMap<>();
            if(isGuest(authentication))
                mapa.put("player","guest");
            else
                mapa.put("player",PlayerRepository.findByUsername(authentication.getName()).playerDTO());
            List<Map<String, Object>> games = gameRepository.findAll().stream().map(Game::gamesDTO).collect(Collectors.toList());
            List<Map<String, Object>> scores = PlayerRepository.findAll().stream().map(Player::playerScoreDTO).collect(Collectors.toList());
            mapa.put("games",games);
            mapa.put("playerScore",scores);
            return mapa;
        }

        @RequestMapping("/gp/{id}")
        public ResponseEntity<Map<String, Object>> getGame_view(@PathVariable Long id, Authentication authentication){
            GamePlayer gp = gamePlayerRepository.findById(id).orElse(null);
            Player player = PlayerRepository.findByUsername(authentication.getName());
            Map<String, Object> error = new HashMap<>();
            if(gp!= null){
                if (player.getGamePlayers().stream().anyMatch(e->e.getId()==id)){
                    return new ResponseEntity<Map<String, Object>>(gp.gameVIewDTO(), HttpStatus.ACCEPTED);
                }else{
                    error.put("error","UNAUTHORIZED");
                    return new ResponseEntity<Map<String, Object>>(error, HttpStatus.UNAUTHORIZED);
                }
            }else{
                error.put("error","no se encontro el juego: "+id);
                return new ResponseEntity<Map<String, Object>>(error, HttpStatus.FORBIDDEN);
            }
        }

        //crear juegos
        @RequestMapping(path ="/games", method = RequestMethod.POST)
        public ResponseEntity<Map<String, Object>> createGame(Authentication authentication){
            Map<String, Object> respuesta = new HashMap<>();
            if(!isGuest(authentication)) {
                Player player = PlayerRepository.findByUsername(authentication.getName());
                if (player != null) {
                    Game game = new Game(0);
                    GamePlayer gamePlayer = new GamePlayer(player, game);
                    gameRepository.save(game);
                    gamePlayerRepository.save(gamePlayer);
                    respuesta.put("gpid", gamePlayer.getId());
                    return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
                } else {
                    respuesta.put("error", "you need to login");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }
            }else{
                respuesta.put("error", "you need to login");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        }

        //unirse a juegos
        @RequestMapping(path = "/game/{id}/players", method = RequestMethod.POST)
        public ResponseEntity<Map<String, Object>> joingame(@PathVariable Long id,Authentication authentication){
            Map<String, Object> respuesta = new HashMap<>();
            if(!isGuest(authentication)) {
                Player player = PlayerRepository.findByUsername(authentication.getName());
                Game game = gameRepository.findById(id).orElse(null);
                if (game == null){
                    respuesta.put("error","No such game");
                    return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
                }
                if(game.getPlayers().size() == 2){
                    respuesta.put("error","Game is full");
                    return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
                }
                GamePlayer gamePlayer = new GamePlayer(player, game);
                gameRepository.save(game);
                gamePlayerRepository.save(gamePlayer);
                respuesta.put("gpid", gamePlayer.getId());
                return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
            }else{
                respuesta.put("error","no hay usuario logeado");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        }

        //registrar players
        @RequestMapping(path = "/players", method = RequestMethod.POST)
        public ResponseEntity<Object> register(
                @RequestParam String firstName, @RequestParam String lastName,
                @RequestParam String email, @RequestParam String password, @RequestParam String username) {

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
            }

            if (PlayerRepository.findByUsername(username) !=  null && PlayerRepository.findByCorreo(email) !=  null) {
                return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
            }

            PlayerRepository.save(new Player(firstName, lastName, email, username, passwordEncoder.encode(password)));
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
}
