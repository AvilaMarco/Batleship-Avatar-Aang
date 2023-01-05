package com.codeblockacademy.shipbender.enums;

public enum Rol {
    GUEST("GUEST"), PLAYER("PLAYER"), ADMIN("ADMIN");

    String rol;

    Rol ( String rol ) {
        this.rol = rol;
    }
}
