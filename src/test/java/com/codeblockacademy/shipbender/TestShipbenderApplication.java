package com.codeblockacademy.shipbender;

import org.springframework.boot.SpringApplication;

public class TestShipbenderApplication {

    public static void main ( String[] args ) {
        SpringApplication.from(SalvoApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
