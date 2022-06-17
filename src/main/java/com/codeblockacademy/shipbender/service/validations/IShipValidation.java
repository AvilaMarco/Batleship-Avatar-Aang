package com.codeblockacademy.shipbender.service.validations;

import com.codeblockacademy.shipbender.models.Ship;

import java.util.List;
import java.util.Set;

public interface IShipValidation {
    void insideTheRange ( List<String> lista );

    void isConsecutive ( Set<Ship> ships );

    void positionsNotRepeated ( List<String> lista );

    void realships ( Set<Ship> ships );
}
