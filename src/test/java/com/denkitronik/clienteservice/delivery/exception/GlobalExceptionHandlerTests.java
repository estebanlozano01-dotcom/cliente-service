package com.denkitronik.clienteservice.delivery.exception;

import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesMissingClientWithId() {
        var response = handler.manejarNotFound(new ClienteNotFoundException(999L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().getMensaje().contains("999"));
    }

    @Test
    void handlesServiceExceptionAsInternalError() {
        var response = handler.manejarServiceException(new ClienteServiceException("internal"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno del servidor. Por favor intenta más tarde.",
                response.getBody().getMensaje());
    }

    @Test
    void handlesWrappedDataIntegrityAsConflict() {
        var exception = new ClienteServiceException("duplicate",
                new DataIntegrityViolationException("duplicate"));

        var response = handler.manejarServiceException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    void handlesDataIntegrityAsConflict() {
        var response = handler.manejarConflicto(
                new DataIntegrityViolationException("duplicate"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handlesFieldValidationErrors() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "cliente");
        bindingResult.addError(new FieldError("cliente", "nombre", "El nombre es obligatorio"));
        var exception = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        var response = handler.manejarValidacion(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMensaje().contains("nombre"));
    }

    @Test
    void handlesMalformedJson() {
        var response = handler.manejarJsonMalformado(
                new HttpMessageNotReadableException("invalid JSON"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("JSON malformado", response.getBody().getMensaje());
    }

    @Test
    void handlesMissingRoute() {
        var response = handler.manejarRutaNoEncontrada(
                new NoResourceFoundException(HttpMethod.GET, "/missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Ruta no encontrada", response.getBody().getMensaje());
    }

    @Test
    void handlesUnexpectedException() {
        var response = handler.manejarExcepcionGenerica(new IllegalStateException("internal"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error inesperado. Por favor contacta al administrador.",
                response.getBody().getMensaje());
    }
}
