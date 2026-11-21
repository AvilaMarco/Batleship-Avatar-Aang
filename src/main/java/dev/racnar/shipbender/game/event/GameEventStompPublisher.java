package dev.racnar.shipbender.game.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Único lugar que conoce destinos /topic; publica solo tras el commit. */
@Component
public class GameEventStompPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public GameEventStompPublisher ( SimpMessagingTemplate messagingTemplate ) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on ( GameEvent e ) {
        messagingTemplate.convertAndSend("/topic/games/" + e.gameId(), e);
    }
}
