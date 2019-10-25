package com.codeoftheweb.salvo.models;

import com.codeoftheweb.salvo.enums.TypeShips;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private TypeShips typeShip;

    @ElementCollection
    private List<String> shipLocations;

    //relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    //constructores
    public Ship(){}

    public Ship(TypeShips typeShip, List<String> shipLocations){
        this.typeShip = typeShip;
        this.shipLocations = shipLocations;
    }

    //gets and sets

    public long getId() {
        return this.id;
    }

    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public void setGamePlayer(GamePlayer GamePlayer){
        this.gamePlayer = GamePlayer;
    }

    public TypeShips getTypeShips() {
        return this.typeShip;
    }

    //data transfer object
    public Map<String,Object> shipsDTO(){
        Map<String,Object> dto = new HashMap<>();
        dto.put("type_Ship",this.typeShip);
        dto.put("locations",this.shipLocations);
        return dto;
    }
}
