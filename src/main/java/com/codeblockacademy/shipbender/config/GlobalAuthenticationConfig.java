package com.codeblockacademy.shipbender.config;

/*

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
