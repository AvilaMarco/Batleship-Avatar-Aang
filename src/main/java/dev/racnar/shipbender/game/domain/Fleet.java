package dev.racnar.shipbender.game.domain;

import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Fleet(List<ShipPlacement> ships) {

    public Fleet {
        if (ships == null) {
            throw invalid("la flota es obligatoria");
        }
        Set<ShipType> missing = EnumSet.allOf(ShipType.class);
        for (ShipPlacement s : ships) {
            if (s == null || s.type() == null) {
                throw invalid("barco sin tipo");
            }
            if (!missing.remove(s.type())) {
                throw invalid(s.type() + " repetido");
            }
        }
        if (!missing.isEmpty()) {
            throw invalid("falta " + missing.iterator().next());
        }
        for (ShipPlacement s : ships) {
            if (s.cells() == null || s.cells().size() != s.type().size()) {
                throw invalid(s.type() + " debe tener " + s.type().size() + " celdas");
            }
        }
        for (ShipPlacement s : ships) {
            checkStraight(s);
        }
        Set<Cell> used = new HashSet<>();
        for (ShipPlacement s : ships) {
            for (Cell c : s.cells()) {
                if (!used.add(c)) {
                    throw invalid(s.type() + " se superpone en " + c);
                }
            }
        }
    }

    private static void checkStraight ( ShipPlacement s ) {
        List<Cell> cs = s.cells().stream()
          .sorted(Comparator.comparingInt(Cell::col).thenComparing(Cell::row))
          .toList();
        boolean sameRow = cs.stream().allMatch(c -> c.row() == cs.get(0).row());
        boolean sameCol = cs.stream().allMatch(c -> c.col() == cs.get(0).col());
        for (int i = 1; i < cs.size(); i++) {
            Cell a = cs.get(i - 1), b = cs.get(i);
            boolean ok = sameRow ? b.col() == a.col() + 1 : sameCol && b.row() == a.row() + 1;
            if (!ok) {
                throw invalid(s.type() + " no es una línea recta de celdas consecutivas en " + b);
            }
        }
    }

    private static ShipbenderException invalid ( String detail ) {
        return new ShipbenderException(ErrorCode.INVALID_FLEET, detail);
    }
}
