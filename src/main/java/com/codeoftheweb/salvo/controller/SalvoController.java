package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.service.main.SalvoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

/*Anotación que le dice a Spring que vamos a hacer un controller que cumple con los requisitos REST.
Es decir, que vamos a utilizar entre otras cosas los métodos GET, POST, PUT, etc.
*/
@RestController
//Anotación que declara como va a ser la estructura de nuestros end-point. Todos van a comenzar con '/api'
@RequestMapping("/api")
public class SalvoController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    SalvoService salvoService;


    //Anotación que le dice a Spring que traiga toda la información y métodos del repositorio o clase a la que
    //hacemos referencia, podemos instanciar un repositorio
    @Autowired
    private com.codeoftheweb.salvo.repository.GameRepository gameRepository;
    @Autowired
    private com.codeoftheweb.salvo.repository.PlayerRepository PlayerRepository;
    @Autowired
    private com.codeoftheweb.salvo.repository.GamePlayerRepository gamePlayerRepository;
    @Autowired
    private com.codeoftheweb.salvo.repository.ScoreRepository ScoreRepository;

//        codificar contraseña
//        passwordEncoder.encode(password)

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private boolean insideTheRange(List<String> lista) {
        return lista.stream()
                .allMatch(location -> location.charAt(0) >= 'A' && location.charAt(0) <= 'J' &&
                        parseInt(location.substring(1)) >= 1 && parseInt(location.substring(1)) <= 10
                );
    }

    private boolean isConsecutive(Set<Ship> ships) {
        //codigo
        List<Boolean> list = new ArrayList<>(Collections.emptyList());
        ships
                .forEach(e -> {
                    for (int i = 0; i < e.getShipLocations().size(); i++) {
                        int aux = i;
                        int aux2 = i + 1;
                        //me fijo que es horizontal
                        if (e.getShipLocations().stream().allMatch(p -> p.charAt(0) == e.getShipLocations().get(aux).charAt(0))
                                && aux2 < e.getShipLocations().size() &&
                                parseInt(e.getShipLocations().get(aux).substring(1)) + 1 == parseInt(e.getShipLocations().get(aux2).substring(1))
                        ) {
                            list.add(true);
                            //es vertical
                        } else if (e.getShipLocations().stream().allMatch(p -> parseInt(p.substring(1)) == parseInt(e.getShipLocations().get(aux).substring(1)))
                                && aux2 < e.getShipLocations().size() &&
                                e.getShipLocations().get(aux).charAt(0) + 1 == e.getShipLocations().get(aux2).charAt(0)
                        ) {
                            list.add(true);
                        } else if (aux2 == e.getShipLocations().size()) {

                        } else {
                            list.add(false);
                        }
                    }
                });
        return list.stream().allMatch(b -> b);
    }

    private boolean positionsNotRepeated(List<String> lista) {
        int sizerOriginal = lista.size();
        Set<String> set = new HashSet<>(lista);
        int sizeReal = set.size();
        return sizerOriginal == sizeReal;
    }

    private boolean realships(Set<Ship> ships) {
        return ships.stream().allMatch(s -> {
            boolean correct = false;
            switch (s.getTypeShip().toString()) {
                case "CARRIER":
                    correct = s.getShipLocations().size() == 5;
                    break;
                case "BATTLESHIP":
                    correct = s.getShipLocations().size() == 4;
                    break;
                case "SUBMARINE":
                case "DESTROYER":
                    correct = s.getShipLocations().size() == 3;
                    break;
                case "PATROL_BOAT":
                    correct = s.getShipLocations().size() == 2;
                    break;
            }
            return correct;
        });
    }

    private void elegirganador(GamePlayer gamepplayer) {
        long migpid = gamepplayer.getId();
        GamePlayer mygp = gamepplayer;
        GamePlayer gpOpponent = gamepplayer.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migpid).findFirst().orElse(null);
        Score mypuntaje = new Score();
        Score opponentpuntaje = new Score();
        if (gpOpponent != null) {
            //empate cuando mis disparos destruyen todos los barcos en el mismo turno que mi oponente hace lo mismo
            //de mi gp obtengo los barcos que destrui
            if (mygp.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5) && gpOpponent.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5)) {
                //empate
                mypuntaje = new Score(mygp.getPlayer(), mygp.getGame(), 1, LocalDateTime.now());
                opponentpuntaje = new Score(gpOpponent.getPlayer(), gpOpponent.getGame(), 1, LocalDateTime.now());
            } else if (mygp.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5)) {
                //destrui todos los barcos de mi oponente y gane
                mypuntaje = new Score(mygp.getPlayer(), mygp.getGame(), 3, LocalDateTime.now());
                opponentpuntaje = new Score(gpOpponent.getPlayer(), gpOpponent.getGame(), 0, LocalDateTime.now());
            } else if (gpOpponent.getSalvos().stream().anyMatch(s -> s.shipsDead() != null && s.shipsDead().size() == 5)) {
                //mi oponente destruyo todos mis barcos y perdi
                mypuntaje = new Score(mygp.getPlayer(), mygp.getGame(), 0, LocalDateTime.now());
                opponentpuntaje = new Score(gpOpponent.getPlayer(), gpOpponent.getGame(), 3, LocalDateTime.now());
            }
            ScoreRepository.save(mypuntaje);
            ScoreRepository.save(opponentpuntaje);
        }
    }

    //Creamos nuestro primer end-point con la dirección '/api/drivers'
    @RequestMapping("/games")
    //Definimos un método para administrar la información que brinda nuestro end-point
    //Como todos los métodos, definimos método de acceso, tipo de dato que retorna, nombre y parámetros.
    //El método ingresa al repositorio de los jugadores y trae toda la información
    //Recorremos esa colección de Drivers y llamamos por cada una a su DTO para almacenar su información
    //en una lista para enviarla a la front.
    public Map<String, Object> getGamesAndPlayers(Authentication authentication) {
        Map<String, Object> mapa = new LinkedHashMap<>();

        if (isGuest(authentication)) mapa.put("player", "guest");
        else mapa.put("player", PlayerRepository.findByEmail(authentication.getName()).get().playerDTO());

        List<Map<String, Object>> games = gameRepository.findAll().stream().map(Game::gamesDTO).collect(Collectors.toList());
        List<Map<String, Object>> scores = PlayerRepository.findAll().stream().map(Player::playerScoreDTO).collect(Collectors.toList());
        mapa.put("games", games);
        mapa.put("playerScore", scores);
        return mapa;
    }

    @RequestMapping("/gp/{id}")
    public ResponseEntity<Map<String, Object>> getGame_view(@PathVariable Long id, Authentication authentication) {
        GamePlayer gp = gamePlayerRepository.findById(id).orElse(null);
        Player player = PlayerRepository.findByEmail(authentication.getName()).get();
        Map<String, Object> error = new HashMap<>();
        if (gp != null && player != null) {
            if (player.getGamePlayers().stream().anyMatch(e -> e.getId() == id)) {
                return new ResponseEntity<>(gp.gameVIewDTO(), HttpStatus.ACCEPTED);
            } else {
                error.put("error", "no es tu juego");
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
            }
        } else {
            error.put("error", "no se encontro el juego: " + id);
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }
    }

    //setear salvo
    @RequestMapping(path = "/games/players/{gamePlayerId}/salvos", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createSalvos(Authentication authentication, @PathVariable Long gamePlayerId, @RequestBody List<String> salvostring) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            Player player = PlayerRepository.findByEmail(authentication.getName()).get();
            if (player != null) {
                GamePlayer gp = gamePlayerRepository.findById(gamePlayerId).orElse(null);
                if (!insideTheRange(salvostring)) {
                    respuesta.put("error", "los salvoes estan fuera de la grilla");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }
                if (gp == null) {
                    respuesta.put("error", "el juego no existe");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }
                if (gp.getPlayer().getId() != player.getId()) {
                    respuesta.put("error", "el jugador no pertenece al juego");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }
                if (!positionsNotRepeated(salvostring)) {
                    respuesta.put("error", "los salvos estan repetidos");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }

                if (!gp.gamestard()) {
                    respuesta.put("error", "el juego no empezo");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }

                //esta parte depende de la cantidaAd de ships
                long myid = gp.getId();
                GamePlayer gpoppoent = gp.getGame().getGamePlayers().stream().filter(gap -> gap.getId() != myid).findFirst().orElse(null);
                if (gpoppoent != null && gp.getTurnOpponent() != 1) {
                    Salvo salvoess = gpoppoent.getSalvos().stream().filter(salvo -> salvo.getTurn() == gp.getTurnOpponent()).findFirst().orElse(null);
                    if (salvoess != null) {
                        if (!(salvostring.size() == (5 - salvoess.shipsDead().size()))) {
                            respuesta.put("error", "no se envio la cantidad correcta de salvoes");
                            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                        }
                    }
                }

                if (gp.getPlayer().getScore(gp.getGame()) != null) {
                    respuesta.put("error", "el juego ya termino");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }

                if (gp.gameover() && gp.getMyTurn() == gp.getTurnOpponent()) {
                    respuesta.put("error", "el juego ya termino");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }

                //logica del juego
                if (gp.getMyTurn() <= gp.getTurnOpponent()) {
                    if (gp.gameover()) {
                        Salvo salvo = new Salvo(salvostring, gp.getSalvos().size() + 1);
                        gp.addSalvo(salvo);
                        gamePlayerRepository.save(gp);
                        //cuando gana el primero en jugar
                        elegirganador(gp);
                        respuesta.put("good", "fin del juego");
                        return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
                    } else {
                        Salvo salvo = new Salvo(salvostring, gp.getSalvos().size() + 1);
                        gp.addSalvo(salvo);
                        gamePlayerRepository.save(gp);
                        if (gp.gameover() && gp.getMyTurn() == gp.getTurnOpponent()) {
                            //cuando gana el ultimo en jugar
                            elegirganador(gp);
                            respuesta.put("good", "fin del juego");
                            return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
                        } else {
                            respuesta.put("good", "nice");
                            return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
                        }
                    }
                } else {
                    respuesta.put("error", "no es tu turno");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }
            } else {
                respuesta.put("error", "el jugador no existe");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        } else {
            respuesta.put("error", "necesitas estar logeado para enviar salvo");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //setear barcos
    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createShips(Authentication authentication, @PathVariable Long gamePlayerId, @RequestBody Set<Ship> ships) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            Player player = PlayerRepository.findByEmail(authentication.getName()).get();
            if (player != null) {
                GamePlayer gp = gamePlayerRepository.findById(gamePlayerId).orElse(null);
                if (gp != null && gp.getPlayer().getId() == player.getId()) {
                    //codigo para crear los barcos y agregarlos a el gameplayer
                    if (ships.size() == 5 && ships.stream().allMatch(e -> e.getTypeShip() != null)) {
                        //dentro del rango
                        if (!insideTheRange(ships.stream().flatMap(s -> s.getShipLocations().stream()).collect(Collectors.toList()))) {
                            respuesta.put("error", "las posiciones estan fuera del rango de la grilla");
                            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                        }
                        //que sean consecutivas
                        if (!isConsecutive(ships)) {
                            respuesta.put("error", "las posiciones de los barcos no son correctas");
                            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                        }
                        //que no se pisen
                        List<String> positionShips = new ArrayList<>();
                        ships.forEach(e -> positionShips.addAll(e.getShipLocations()));
                        if (!positionsNotRepeated(positionShips)) {
                            respuesta.put("error", "las posiciones de los barcos se pisan, hay posiciones repetidas");
                            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                        }
                        if (!realships(ships)) {
                            respuesta.put("error", "la cantidad de posiciones en algun barco no es correcta");
                            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                        }
                        if (gp.getShips().size() != 0) {
                            respuesta.put("error", "ya tienes barcos cargados");
                            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                        }
                        ships.forEach(gp::addShip);
                    } else {
                        respuesta.put("error", "el tipo de barco o la cantidad no es correcta");
                        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                    }
                    gamePlayerRepository.save(gp);
                    return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
                } else {
                    respuesta.put("error", "no perteneces al juego");
                    return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
                }
            } else {
                respuesta.put("error", "no estas registrado");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        } else {
            respuesta.put("error", "you need to login");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //setear nacion player
    @RequestMapping(path = "/setNacionPlayer/{nacion}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setnacion(Authentication authentication, @PathVariable String nacion) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            Player player = PlayerRepository.findByEmail(authentication.getName()).get();
            if (player != null) {
                player.setNation(nacion);
                PlayerRepository.save(player);
                respuesta.put("good", "nice");
                return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
            } else {
                respuesta.put("error", "you need to login");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        } else {
            respuesta.put("error", "you need to login");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //setear emote player
    @RequestMapping(path = "/emote/{id}/{emote}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setemote(Authentication authentication, @PathVariable Long id, @PathVariable String emote) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            GamePlayer gp = gamePlayerRepository.findById(id).orElse(null);
            if (gp != null) {
                gp.setEmote(emote);
                gamePlayerRepository.save(gp);
                respuesta.put("good", "nice");
                return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
            } else {
                respuesta.put("error", "you need to login");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        } else {
            respuesta.put("error", "you need to login");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //setear rematch player
    @RequestMapping(path = "/rematch/{id}/{rematch}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setRematch(Authentication authentication, @PathVariable Long id, @PathVariable Boolean rematch) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            GamePlayer gp = gamePlayerRepository.findById(id).orElse(null);
            if (gp != null) {
                gp.setRematch(rematch);
                gamePlayerRepository.save(gp);
                respuesta.put("good", "nice");
                return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
            } else {
                respuesta.put("error", "you need to login");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        } else {
            respuesta.put("error", "you need to login");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //setear setNewGameId
    @RequestMapping(path = "/newGameId/{newgameid}/{gpid}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setNewGameId(@PathVariable Long newgameid, @PathVariable Long gpid) {
        Map<String, Object> respuesta = new HashMap<>();
        GamePlayer gp = gamePlayerRepository.findById(gpid).orElse(null);
        if (gp != null) {
            gp.setnewGameId(newgameid);
            gamePlayerRepository.save(gp);
            respuesta.put("good", "nice");
            return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
        } else {
            respuesta.put("error", "you need to login");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //crear juegos
    @RequestMapping(path = "/games/{ubicacion}/{direccion}/{isrematch}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication, @PathVariable String ubicacion, @PathVariable String direccion, @PathVariable Boolean isrematch) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            Player player = PlayerRepository.findByEmail(authentication.getName()).get();
            Game gameAux = gameRepository.findByDirection(direccion);
            if (gameAux != null && !isrematch) {
                respuesta.put("error", "game already");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
            if (player != null) {
                Game game = new Game(0, ubicacion, direccion);
                GamePlayer gamePlayer = new GamePlayer(player, game);
                PlayerRepository.save(player);
                gameRepository.save(game);
                gamePlayerRepository.save(gamePlayer);
                respuesta.put("gpid", gamePlayer.getId());
                respuesta.put("gameid", game.getId());
                return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
            } else {
                respuesta.put("error", "you need to login");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        } else {
            respuesta.put("error", "you need to login");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //unirse a juegos
    @RequestMapping(path = "/game/{id}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joingame(@PathVariable Long id, Authentication authentication) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            Player player = PlayerRepository.findByEmail(authentication.getName()).get();
            Game game = gameRepository.findById(id).orElse(null);
            if (game == null) {
                respuesta.put("error", "No such game");
                return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
            }
            if (game.getPlayers().size() == 2) {
                respuesta.put("error", "Game is full");
                return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
            }
            if (game.getPlayers().stream().anyMatch(e -> e.getId() == player.getId())) {
                respuesta.put("error", "you are already in the game");
                return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
            }
            GamePlayer gamePlayer = new GamePlayer(player, game);
            gameRepository.save(game);
            gamePlayerRepository.save(gamePlayer);
            respuesta.put("gpid", gamePlayer.getId());
            return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
        } else {
            respuesta.put("error", "no hay usuario logeado");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }

    //registrar players
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String firstName,
            @RequestParam String email, @RequestParam String password) {

        Map<String, Object> respuesta = new HashMap<>();
        if (firstName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            respuesta.put("error", "Missing data");
            return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
        }

        if (PlayerRepository.findByEmail(email) != null) {
            respuesta.put("error", "Name already in use");
            return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
        }
        PlayerRepository.save(new Player(firstName, email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
