package com.codeblockacademy.shipbender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDataDTO {
    GameMatchDTO  data;
    StatusGameDTO status;
}
