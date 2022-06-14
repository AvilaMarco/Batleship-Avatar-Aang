package com.codeblockacademy.shipbender.models;

import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @Enumerated(EnumType.STRING)
    private NationType nation;

    @ElementCollection
    private List<Rol> roles;
    private String    name;
    private String    email;
    private String    password;

    // Relations
    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<GamePlayer> gamePlayers;

    public Player ( String name, String email, String password, NationType nation ) {
        this.nation   = nation;
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    public List<Integer> getScores () {
        return gamePlayers.stream()
          .map(GamePlayer::getScore)
          .filter(score -> !Objects.isNull(score))
          .collect(Collectors.toList());
    }

    public List<String> getRolls () {
        return this.getRoles()
          .stream()
          .map(Enum::name)
          .collect(Collectors.toList());
    }

    public void addRol ( Rol rol ) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(rol);
    }
}