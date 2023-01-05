### ----------------------------------------------------------------- ############### ---------------------------------- #####

public Map<String, Object> gamesDTO() {
Map<String, Object> dto = new LinkedHashMap<>();
dto.put("id", this.id);
dto.put("created", this.created);
dto.put("ubicacion", this.location);
dto.put("direccion", this.direction);
dto.put("gameplayers", this.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO));
return dto;
}

### ----------------------------------------------------------------- ############### ---------------------------------- #####

//dto
public Map<String, Object> gamePlayerDTO() {
Map<String, Object> dto = new HashMap<>();
dto.put("id", this.id);
dto.put("tipo", this.type);
dto.put("player", this.player.playerDTO());
dto.put("Score", this.getScore());
return dto;
}

    public Map<String, Object> gameVIewDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getGame().getId());
        dto.put("ubicacion", this.getGame().getubicacion());
        dto.put("direccion", this.getGame().getDirection());
        dto.put("tipo", this.type);
        dto.put("created", this.joinDate);

        dto.put("Game_Started", this.gamestard());
        dto.put("Game_Over", this.gameover());
        dto.put("my_turn", this.getMyTurn());
        dto.put("Opponent_turn", this.getTurnOpponent());
        dto.put("my_emote", this.getEmote());
        dto.put("Opponent_emote", this.getEmoteOpponent());
        dto.put("my_rematch", this.getRematch());
        dto.put("Opponent_rematch", this.getRematchOpponent());
        if (this.getnewGameIdOpponent() != null) {
            dto.put("new_game", this.getnewGameIdOpponent());
        }
        dto.put("gamePlayers", this.game.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO));
        dto.put("ships", this.getShips().stream().map(Ship::shipsDTO));
        dto.put("salvos", this.getGame().getGamePlayers().stream().flatMap(gp -> gp.getSalvos().stream().map(Salvo::salvoDTO)));
        return dto;
    }

### ----------------------------------------------------------------- ############### ---------------------------------- #####    

        //dto
    public Map<String, Object> playerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("name", this.name);
        dto.put("email", this.email);
        dto.put("nacion", this.nation);
        return dto;
    }

    public Map<String, Object> playerScoreDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.id);
        dto.put("name", this.name);
        dto.put("email", this.email);
        dto.put("scores", this.scores.stream().map(Score::getScore));
        return dto;
    }

### ----------------------------------------------------------------- ############### ---------------------------------- #####      

        //data transfer object
    public Map<String, Object> shipsDTO() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("type_Ship", this.getTypeShip());
        dto.put("locations", this.shipLocations);
        return dto;
    }

    public Map<String, Object> shipstypeDTO() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("type_Ship", this.getTypeShip());
        return dto;
    }

### ----------------------------------------------------------------- ############### ---------------------------------- #####  

    //dto
    public Map<String, Object> salvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", this.turn);
        dto.put("player", this.getIdPlayer());
        dto.put("nice_shoot", this.goodShoot(this.salvoLocations));
        if (this.shipsDead() != null) {
            dto.put("ships_dead", this.shipsDead().stream().map(Ship::shipstypeDTO).collect(Collectors.toList()));
        } else {
            dto.put("ships_dead", this.shipsDead());
        }
        dto.put("locations", this.salvoLocations);
        return dto;
    }
