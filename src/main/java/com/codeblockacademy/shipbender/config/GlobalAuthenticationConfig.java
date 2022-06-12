/*
package com.codeblockacademy.shipbender.config;


import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.repository.PlayerRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class GlobalAuthenticationConfig extends GlobalAuthenticationConfigurerAdapter {
    PasswordEncoder  passwordEncoder;
    PlayerRepository playerRepository;

    public GlobalAuthenticationConfig ( PasswordEncoder passwordEncoder, PlayerRepository playerRepository ) {
        this.passwordEncoder  = passwordEncoder;
        this.playerRepository = playerRepository;
    }

    @Override
    public void init ( AuthenticationManagerBuilder auth ) throws Exception {
        auth.userDetailsService(inputName -> {
            Player player = playerRepository.findByEmail(inputName)
              .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + inputName));

            String name = player.getEmail();
            String pass = player.getPassword();
            List<GrantedAuthority> roles = player.getRolls()
              .stream()
              .map(AuthorityUtils::commaSeparatedStringToAuthorityList)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());

            return new User(name, pass, roles);
        });
    }
}

*/
