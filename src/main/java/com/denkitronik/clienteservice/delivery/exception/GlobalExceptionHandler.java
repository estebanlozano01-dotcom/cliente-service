package com.denkitronik.clienteservice.delivery.exception;

import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 404 Not Found ─────────────────────────────────────────
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> manejarNotFound(
            ClienteNotFoundException ex) {

        log.warn("Cliente no encontrado: id={}", ex.getId());

        ApiErrorResponse respuesta = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    // ── 500 Internal Server Error ─────────────────────────────
    @ExceptionHandler(ClienteServiceException.class)
    public ResponseEntity<ApiErrorResponse> manejarServiceException(
            ClienteServiceException ex) {

        if (causedByDataIntegrityViolation(ex)) {
            return respuestaConflicto(ex);
        }

        log.error("Error en el servicio de clientes", ex);
        ApiErrorResponse respuesta = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Error interno del servidor. Por favor intenta más tarde."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarConflicto(
            DataIntegrityViolationException ex) {

        return respuestaConflicto(ex);
    }

    private ResponseEntity<ApiErrorResponse> respuestaConflicto(Throwable ex) {
        log.warn("Conflicto de integridad al guardar un cliente", ex);
        ApiErrorResponse respuesta = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "El cliente contiene datos que ya existen o infringen una restricción."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    // ── 400 Bad Request (validaciones @Valid) ─────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage()));

        log.debug("Errores de validación: {}", errores);
        return respuestaValidacion(errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> errores = new TreeMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errores.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        log.debug("Errores de validación: {}", errores);
        return respuestaValidacion(errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> manejarJsonMalformado(
            HttpMessageNotReadableException ex) {

        return respuesta(HttpStatus.BAD_REQUEST, "Bad Request", "JSON malformado");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> manejarRutaNoEncontrada(
            NoResourceFoundException ex) {

        return respuesta(HttpStatus.NOT_FOUND, "Not Found", "Ruta no encontrada");
    }

    private ResponseEntity<ApiErrorResponse> respuestaValidacion(Map<String, String> errores) {
        String mensaje = errores.entrySet().stream()
                .map(error -> error.getKey() + ": " + error.getValue())
                .collect(Collectors.joining("; "));
        ApiErrorResponse respuesta = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                mensaje
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    private ResponseEntity<ApiErrorResponse> respuesta(
            HttpStatus status, String error, String mensaje) {
        ApiErrorResponse respuesta = new ApiErrorResponse(status.value(), error, mensaje);
        return ResponseEntity.status(status).body(respuesta);
    }

    private boolean causedByDataIntegrityViolation(Throwable ex) {
        Throwable cause = ex.getCause();
        while (cause != null && cause != ex) {
            if (cause instanceof DataIntegrityViolationException) {
                return true;
            }
            ex = cause;
            cause = cause.getCause();
        }
        return false;
    }

    // ── 500 genérico para cualquier excepción no manejada ─────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarExcepcionGenerica(
            Exception ex) {

        log.error("Excepción no manejada", ex);

        ApiErrorResponse respuesta = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Error inesperado. Por favor contacta al administrador."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}