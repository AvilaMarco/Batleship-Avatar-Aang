package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.models.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        //hacemos referencia
        @Autowired
        private com.codeoftheweb.salvo.repository.GameRepository gameRepository;

        @Autowired
        private com.codeoftheweb.salvo.repository.GamePlayerRepository gamePlayerRepository;

        //Creamos nuestro primer end-point con la dirección '/api/drivers'
        @RequestMapping("/games")
        //Definimos un método para administrar la información que brinda nuestro end-point
        //Como todos los métodos, definimos método de acceso, tipo de dato que retorna, nombre y parámetros.
        //El método ingresa al repositorio de los jugadores y trae toda la información
        //Recorremos esa colección de Drivers y llamamos por cada una a su DTO para almacenar su información
        //en una lista para enviarla a la front.
        public List<Map<String, Object>> getGames(){
            return gameRepository.findAll().stream().map(Game::gamesDTO).collect(Collectors.toList());
        }

        @RequestMapping("/gp/{id}")
        public Map<String, Object> getGame_view(@PathVariable Long id){
                return gamePlayerRepository.getOne(id).gameVIewDTO();
        }
}
