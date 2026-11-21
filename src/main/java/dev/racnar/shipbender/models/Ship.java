package dev.racnar.shipbender.models;

import dev.racnar.shipbender.enums.ShipType;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
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
