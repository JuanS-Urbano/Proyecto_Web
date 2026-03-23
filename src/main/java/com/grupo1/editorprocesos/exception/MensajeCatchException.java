package com.grupo1.editorprocesos.exception;

/**
 * Excepción lanzada cuando catchMessage() detecta un estado inválido:
 * mensaje duplicado, proceso destino inactivo, o datos inconsistentes.
 *
 * Registrada en GlobalExceptionHandler con HTTP 409 CONFLICT,
 * siguiendo el patrón de DuplicateResourceException ya existente.
 */
public class MensajeCatchException extends RuntimeException {

    public MensajeCatchException(String message) {
        super(message);
    }

    public MensajeCatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
