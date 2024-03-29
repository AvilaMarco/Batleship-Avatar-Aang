package com.codeblockacademy.shipbender.dto;

import com.codeblockacademy.shipbender.enums.NationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {

    Long   id;
    String name;
    String email;

    @Enumerated(EnumType.STRING)
    NationType nation;

    public PlayerDTO ( String name ) {
        this.name = name;
    }
}
