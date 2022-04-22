package com.codeoftheweb.salvo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusGameDTO {
    String status;
    Long gpPlayerOneId;
    Long gpPlayerTwoId;
}
