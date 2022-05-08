package com.codeoftheweb.salvo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusGameDTO {
    String status;
    Long   gpPlayerOneId;
    Long   gpPlayerTwoId;
}
