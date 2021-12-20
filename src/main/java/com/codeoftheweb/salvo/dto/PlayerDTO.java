package com.codeoftheweb.salvo.dto;

import com.codeoftheweb.salvo.enums.NationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PlayerDTO {

    Long id;
    String name;
    String email;

    @Enumerated(EnumType.STRING)
    NationType nation;

    public PlayerDTO(String name){
        this.name = name;
    }
}
