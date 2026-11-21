package dev.racnar.shipbender.game.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
public class ErrorDTO {

    String                        error;
    String                        message;
    HashMap<String, List<String>> errorFields;

    public ErrorDTO ( String error, String message ) {
        this.error   = error;
        this.message = message;
    }
}
