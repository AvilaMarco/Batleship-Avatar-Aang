package com.codeoftheweb.salvo.service.main;

import com.codeoftheweb.salvo.dto.response.UserTokenDTO;
import com.codeoftheweb.salvo.exception.not_found.LoginException;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import com.codeoftheweb.salvo.service.intereface.ISessionService;
import com.codeoftheweb.salvo.utils.ENV_VARIABLES;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyStoreException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService implements ISessionService, ENV_VARIABLES {

    PlayerRepository playerRepository;
    PasswordEncoder  passwordEncoder;

    public SessionService ( PlayerRepository playerRepository, PasswordEncoder passwordEncoder ) {
        this.playerRepository = playerRepository;
        this.passwordEncoder  = passwordEncoder;
    }

    @Override
    public UserTokenDTO login ( String username, String password ) throws KeyStoreException {
        //Voy a la base de datos y reviso que el usuario y contraseña existan.
        Player player = playerRepository
          .findByEmail(username)
          .orElseThrow(LoginException::new);

        if (passwordEncoder.matches(password, player.getPassword())) {
            throw new LoginException();
        }

        String token = getJWTToken(username, player.getRols());
        return new UserTokenDTO(username, token);
    }

    /**
     * Genera un token para un usuario específico, válido por 10'
     *
     * @param username - user to login
     * @param roles    - collection of user's roles
     * @return String
     */
    /* lista de roles */
    private String getJWTToken ( String username, List<String> roles ) throws KeyStoreException {

        List<String> authorities = roles
          .stream()
          .map(AuthorityUtils::commaSeparatedStringToAuthorityList)
          .flatMap(Collection::stream)
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toList());

        LocalDateTime expired = LocalDateTime.now()
          .plusMinutes(MINUTES_TOKEN_EXPIRATION);
        Date expiredTime = Date.from(expired.atZone(ZoneId.systemDefault())
          .toInstant());

        String token = Jwts
          .builder()
          .setId("softtekJWT")
          .setSubject(username)
          .claim(CLAIMS, authorities)
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(expiredTime)
          .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
          .compact();

        return PREFIX + token;
    }

    /**
     * Decodifica un token para poder obtener los componentes que contiene el mismo
     *
     * @param token tokenJWT
     * @return Claims
     */
    private static Claims decodeJWT ( String token ) {
        return Jwts.parser()
          .setSigningKey(SECRET_KEY)
          .parseClaimsJws(token)
          .getBody();
    }

    /**
     * Permite obtener el username según el token indicado
     *
     * @param token token JWT
     * @return String User's Email
     */
    public static String getUsername ( String token ) {
        Claims claims = decodeJWT(token);
        return claims.get("sub", String.class);
    }

}
