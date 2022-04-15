package com.codeoftheweb.salvo.models;

import com.codeoftheweb.salvo.enums.NationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @Enumerated(EnumType.STRING)
    private NationType nation;
    private String     name;
    private String     email;
    private String     password;

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
}