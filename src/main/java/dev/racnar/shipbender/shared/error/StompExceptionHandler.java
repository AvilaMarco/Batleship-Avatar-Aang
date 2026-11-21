package dev.racnar.shipbender.shared.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Errores de mensajes STOMP al /user/queue/errors de quien los envió (ADR 004 Anexo C). */
@ControllerAdvice
public class StompExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StompExceptionHandler.class);

    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    ProblemDetail handle ( Exception ex, SimpMessageHeaderAccessor headers ) {
        ProblemDetail pd;
        if (ex instanceof ShipbenderException e) {
            pd = detail(e.code(), e.getMessage());
        } else if (ex instanceof MethodArgumentNotValidException e) {
            pd = detail(ErrorCode.VALIDATION_FAILED, "Hay campos que no cumplen las validaciones");
            Map<String, List<String>> errors = new LinkedHashMap<>();
            e.getBindingResult().getFieldErrors()
              .forEach(f -> errors.computeIfAbsent(f.getField(), k -> new ArrayList<>()).add(f.getDefaultMessage()));
            pd.setProperty("errors", errors);
        } else {
            log.error("Error no esperado en mensaje STOMP", ex);
            pd = detail(ErrorCode.INTERNAL_ERROR, "Error interno");
        }
        pd.setInstance(URI.create(String.valueOf(headers.getDestination())));
        return pd;
    }

    private static ProblemDetail detail ( ErrorCode code, String message ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(code.status()), message);
        pd.setProperty("code", code.name());
        return pd;
    }
}
