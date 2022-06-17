package com.codeblockacademy.shipbender.controller;

import com.codeblockacademy.shipbender.dto.response.MenuViewDTO;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.models.Ship;
import com.codeblockacademy.shipbender.repository.GamePlayerRepository;
import com.codeblockacademy.shipbender.repository.GameRepository;
import com.codeblockacademy.shipbender.service.intereface.IMatchService;
import com.codeblockacademy.shipbender.service.intereface.ISalvoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/games")
public class SalvoController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ISalvoService salvoService;

    @Autowired
    IMatchService matchService;

    // listar info juegos
    @GetMapping
    public MenuViewDTO getGamesAndPlayers ( Authentication authentication ) {
        return salvoService.getInfoGames(authentication);
    }

    //Anotación que le dice a Spring que traiga toda la información y métodos del repositorio o clase a la que
    //hacemos referencia, podemos instanciar un repositorio
    @Autowired
    private GameRepository                                              gameRepository;
    @Autowired
    private com.codeblockacademy.shipbender.repository.PlayerRepository PlayerRepository;
    @Autowired
    private GamePlayerRepository                                        gamePlayerRepository;


    private boolean isGuest ( Authentication authentication ) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private void elegirganador ( GamePlayer gamepplayer ) {
        long       migpid = gamepplayer.getId();
        GamePlayer mygp   = gamepplayer;
        GamePlayer gpOpponent = gamepplayer.getGame()
          .getGamePlayers()
          .stream()
          .filter(gamep -> gamep.getId() != migpid)
          .findFirst()
          .orElse(null);
        /*if (gpOpponent != null) {
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
        }*/
    }

    @GetMapping(path = "/gp/{id}")
    public ResponseEntity<Map<String, Object>> getGame_view ( Authentication authentication, @PathVariable Long id ) {
        GamePlayer gp = gamePlayerRepository.findById(id)
          .orElse(null);
        Player player = PlayerRepository.findByEmail(authentication.getName())
          .get();
        Map<String, Object> error = new HashMap<>();
        if (gp != null && player != null) {
            if (player.getGamePlayers()
              .stream()
              .anyMatch(e -> e.getId() == id)) {
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
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
    public ResponseEntity<Map<String, Object>> createSalvos ( Authentication authentication, @PathVariable Long gamePlayerId, @RequestBody List<String> salvostring ) {
        Map<String, Object> respuesta = new HashMap<>();
       /* if (!isGuest(authentication)) {
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
        } else {*/
        respuesta.put("error", "necesitas estar logeado para enviar salvo");
        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        /*}*/
    }

    //setear barcos
    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createShips ( Authentication authentication, @PathVariable Long gamePlayerId, @RequestBody Set<Ship> ships ) {
        Map<String, Object> respuesta = new HashMap<>();
/*        if (!isGuest(authentication)) {
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
        }*/
        respuesta.put("error", "you need to login");
        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
    }


    //setear emote player
    @RequestMapping(path = "/emote/{id}/{emote}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setemote ( Authentication authentication, @PathVariable Long id, @PathVariable String emote ) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            GamePlayer gp = gamePlayerRepository.findById(id)
              .orElse(null);
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


}
