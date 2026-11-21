package dev.racnar.shipbender.shared.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Formato de error único (RFC 9457) con `code` estable; ver ADR 004 Anexo C. */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ShipbenderException.class)
    ResponseEntity<ProblemDetail> shipbender ( ShipbenderException ex ) {
        return problem(ex.code(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected ( Exception ex ) {
        log.error("Error no esperado", ex);
        return problem(ErrorCode.INTERNAL_ERROR, "Error interno");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid ( MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request ) {
        ProblemDetail pd = detail(ErrorCode.VALIDATION_FAILED, "Hay campos que no cumplen las validaciones");
        Map<String, List<String>> errors = new LinkedHashMap<>();
        ex.getFieldErrors().forEach(e -> errors.computeIfAbsent(e.getField(), k -> new ArrayList<>()).add(e.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable ( HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request ) {
        return ResponseEntity.badRequest().body(detail(ErrorCode.MALFORMED_REQUEST, "El cuerpo de la petición es ilegible o tiene un valor desconocido"));
    }

    private static ProblemDetail detail ( ErrorCode code, String message ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(code.status()), message);
        pd.setProperty("code", code.name());
        return pd;
    }

    private static ResponseEntity<ProblemDetail> problem ( ErrorCode code, String message ) {
        return ResponseEntity.status(code.status()).body(detail(code, message));
    }
}
