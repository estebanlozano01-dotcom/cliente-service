package com.denkitronik.clienteservice.delivery.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private int status;
    private String error;
    private String mensaje;
    private LocalDateTime timestamp;
    private Map<String, String> errores;
    private List<String> errors;

    public ApiErrorResponse(int status, String error, String mensaje) {
        this.status    = status;
        this.error     = error;
        this.mensaje   = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    public ApiErrorResponse(int status, String error, String mensaje,
                            Map<String, String> errores) {
        this(status, error, mensaje);
        this.errores = errores;
        this.errors = errores.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .toList();
    }

    public int getStatus()               { return status; }
    public String getError()             { return error; }
    public String getMensaje()           { return mensaje; }
    public LocalDateTime getTimestamp()  { return timestamp; }
    public Map<String, String> getErrores() { return errores; }
    public List<String> getErrors() { return errors; }
}