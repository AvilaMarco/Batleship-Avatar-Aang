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

    private TypeShips typeShips;

    @ElementCollection
    private List<String> shipLocation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    //constructores
    public Ship(){}

    public Ship(TypeShips typeShips,List<String> shipLocation){
        this.typeShips = typeShips;
        this.shipLocation = shipLocation;
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
        return this.typeShips;
    }

    //data transfer object
    public Map<String,Object> shipsDTO(){
        Map<String,Object> dto = new HashMap<>();
        dto.put("type_Ship",this.typeShips);
        dto.put("locations",this.shipLocation);
        return dto;
    }
}
