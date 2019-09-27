package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {

		SpringApplication.run(SalvoApplication.class, args);
	}

    @Bean
    public CommandLineRunner initData(PlayerRepository PlayerRepository) {
        return (args) -> {
            // save a couple of customers
            PlayerRepository.save(new Player("Jack", "Bauer","j.bauer@ctu.gov"));
            PlayerRepository.save(new Player("Chloe", "O'Brian","c.obrian@ctu.gov"));
            PlayerRepository.save(new Player("Kim", "Bauer","kim_bauer@gmail.com"));
            PlayerRepository.save(new Player("Tony", "Almeida","t.almeida@ctu.gov"));
        };
    }
}
