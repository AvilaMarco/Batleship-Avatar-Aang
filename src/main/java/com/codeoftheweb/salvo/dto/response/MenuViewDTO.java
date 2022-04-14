package com.codeoftheweb.salvo.dto.response;

import com.codeoftheweb.salvo.dto.GameMapDTO;
import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.PlayerScoreDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuViewDTO {

    PlayerDTO player;
    List<GameMapDTO> games;
    List<PlayerScoreDTO> players;

}
