//package com.codeblockacademy.shipbender.dto;
//
//import com.codeblockacademy.shipbender.enums.ShipType;
//import org.hibernate.annotations.GenericGenerator;
//
//import javax.persistence.*;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Entity
//public class ShipsDTO {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
//    @GenericGenerator(name = "native", strategy = "native")
//    private long id;
//
//    private ShipType typeShip;
//
//    @ElementCollection
//    private List<String> shipLocations;
//
//    //constructores
//    public ShipsDTO () {
//    }
//
//    public ShipsDTO ( ShipType typeShip, List<String> shipLocations ) {
//        this.typeShip      = typeShip;
//        this.shipLocations = shipLocations;
//    }
//
//    //gets and sets
//
//    public List<String> getShipLocations () {
//        return shipLocations;
//    }
//
//    public long getId () {
//        return this.id;
//    }
//
//    public ShipType getTypeShips () {
//        return this.typeShip;
//    }
//
//    //data transfer object
//    public Map<String, Object> shipsDTO () {
//        Map<String, Object> dto = new HashMap<>();
//        dto.put("type_Ship", this.typeShip);
//        dto.put("locations", this.shipLocations);
//        return dto;
//    }
//}