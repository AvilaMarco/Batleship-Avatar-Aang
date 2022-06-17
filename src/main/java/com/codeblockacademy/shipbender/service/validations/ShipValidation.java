package com.codeblockacademy.shipbender.service.validations;

import com.codeblockacademy.shipbender.models.Ship;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.lang.Integer.parseInt;

@Service
public class ShipValidation implements IShipValidation {

    @Override
    public void insideTheRange ( List<String> lista ) {
        lista.stream()
          .allMatch(location -> location.charAt(0) >= 'A' && location.charAt(0) <= 'J' &&
            parseInt(location.substring(1)) >= 1 && parseInt(location.substring(1)) <= 10
          );
    }

    @Override
    public void isConsecutive ( Set<Ship> ships ) {
        //codigo
        List<Boolean> list = new ArrayList<>(Collections.emptyList());
//        ships
//                .forEach(e -> {
//                    for (int i = 0; i < e.getShipLocations().size(); i++) {
//                        int aux = i;
//                        int aux2 = i + 1;
//                        //me fijo que es horizontal
//                        if (e.getShipLocations().stream().allMatch(p -> p.charAt(0) == e.getShipLocations().get(aux).charAt(0))
//                                && aux2 < e.getShipLocations().size() &&
//                                parseInt(e.getShipLocations().get(aux).substring(1)) + 1 == parseInt(e.getShipLocations().get(aux2).substring(1))
//                        ) {
//                            list.add(true);
//                            //es vertical
//                        } else if (e.getShipLocations().stream().allMatch(p -> parseInt(p.substring(1)) == parseInt(e.getShipLocations().get(aux).substring(1)))
//                                && aux2 < e.getShipLocations().size() &&
//                                e.getShipLocations().get(aux).charAt(0) + 1 == e.getShipLocations().get(aux2).charAt(0)
//                        ) {
//                            list.add(true);
//                        } else if (aux2 == e.getShipLocations().size()) {
//
//                        } else {
//                            list.add(false);
//                        }
//                    }
//                });
//        return list.stream()
//          .allMatch(b -> b);
    }

    @Override
    public void positionsNotRepeated ( List<String> lista ) {
        int         sizerOriginal = lista.size();
        Set<String> set           = new HashSet<>(lista);
        int         sizeReal      = set.size();
//        return sizerOriginal == sizeReal;
    }

    @Override
    public void realships ( Set<Ship> ships ) {
        ships.stream()
          .allMatch(s -> {
              boolean correct = true;
/*            switch (s.getTypeShip().toString()) {
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
            }*/
              return correct;
          });
    }
}
