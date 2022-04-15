package com.codeoftheweb.salvo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ElementCollection
    private List<String> locations;
    private int          turn;

    //relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    //otros metodos
    @JsonIgnore
    public List<String> goodShoot ( List<String> shoots ) {
        long       migp = this.gamePlayer.getId();
        GamePlayer gp   = this.gamePlayer.getGame().getGamePlayers().stream().filter(gamep -> gamep.getId() != migp).findFirst().orElse(null);
        if (gp != null) {
            List<String> positionShips = gp.getShips().stream().flatMap(e -> e.getLocations().stream().map(l -> l)).collect(Collectors.toList());
            return shoots.stream().filter(s -> positionShips.stream().anyMatch(p -> p.equals(s))).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @JsonIgnore
    public List<Ship> shipsDead () {
        List<String> salvosposition = new ArrayList<>();
        this.gamePlayer.getSalvos().stream()
          .filter(salvo -> salvo.getTurn() <= this.getTurn())
          .forEach(salvo -> salvosposition.addAll(salvo.locations));
        long migp = this.gamePlayer.getId();
        GamePlayer gp = this.gamePlayer.getGame().getGamePlayers().stream()
          .filter(gamep -> gamep.getId() != migp)
          .findFirst()
          .orElse(null);

        if (gp != null) {
            List<Ship> ships     = new ArrayList<>(gp.getShips());
            List<Ship> shipsDead = new ArrayList<>(ships.stream().filter(s -> s.getLocations().stream().allMatch(position -> salvosposition.stream().anyMatch(sp -> sp.equals(position)))).collect(Collectors.toList()));
            if (shipsDead.size() != 0) {
                return shipsDead;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
