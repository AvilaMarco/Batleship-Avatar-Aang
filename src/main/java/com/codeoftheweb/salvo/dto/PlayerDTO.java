package com.codeoftheweb.salvo.dto;

import com.codeoftheweb.salvo.enums.NationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {

    Long id;
    String name;
    String email;

    @Enumerated(EnumType.STRING)
    NationType nation;
}
