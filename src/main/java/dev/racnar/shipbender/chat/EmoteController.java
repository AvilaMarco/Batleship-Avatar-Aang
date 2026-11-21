package dev.racnar.shipbender.chat;

import dev.racnar.shipbender.chat.dto.EmoteRequest;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class EmoteController {

    private final ChatService service;

    public EmoteController ( ChatService service ) {
        this.service = service;
    }

    @MessageMapping("/games/{gameId}/emotes")
    public void emote ( @DestinationVariable long gameId, @Payload @Valid EmoteRequest request, Principal principal ) {
        service.sendEmote(principal.getName(), gameId, request.emote());
    }
}
