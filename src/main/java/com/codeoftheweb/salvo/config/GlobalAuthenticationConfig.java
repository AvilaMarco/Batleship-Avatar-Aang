/*
package com.codeoftheweb.salvo.config;

import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class GlobalAuthenticationConfig extends GlobalAuthenticationConfigurerAdapter {

    //cuando el usuario llega se codifica la contraseña y compara la String resultante con la
    //contraseña codificada que exite
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public void init ( AuthenticationManagerBuilder auth ) throws Exception {
        auth.userDetailsService(inputName -> {
            Player player = playerRepository.findByEmail(inputName)
              .orElse(null);

            if (player == null) throw new UsernameNotFoundException("Unknown user: " + inputName);

            String name = player.getEmail();
            String pass = player.getPassword();
            String rol = player.getEmail()
              .equals("racnar1") ? "ADMIN" : "USER";

            return new User(name, pass, AuthorityUtils.createAuthorityList(rol));
        });
    }
}*/
