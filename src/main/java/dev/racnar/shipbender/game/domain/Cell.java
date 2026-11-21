package dev.racnar.shipbender.game.domain;

import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Cell(char row, int col) {

    private static final Pattern FORMAT = Pattern.compile("^([A-J])([1-9]|10)$");

    public static Cell parse ( String s ) {
        Matcher m = s == null ? null : FORMAT.matcher(s);
        if (m == null || !m.matches()) {
            throw new ShipbenderException(ErrorCode.INVALID_FLEET, "celda inválida: " + s);
        }
        return new Cell(m.group(1).charAt(0), Integer.parseInt(m.group(2)));
    }

    @Override
    public String toString () {
        return "" + row + col;
    }
}
