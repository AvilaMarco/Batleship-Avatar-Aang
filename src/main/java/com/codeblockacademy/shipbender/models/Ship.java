package com.codeblockacademy.shipbender.models;

import com.codeblockacademy.shipbender.enums.ShipType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> locations;

    @ElementCollection
    private List<String> salvosImpacted;

    @Enumerated(EnumType.STRING)
    private ShipType type;

    // Relations
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "gamePlayer_id")
//    private GamePlayer gamePlayer;

}
