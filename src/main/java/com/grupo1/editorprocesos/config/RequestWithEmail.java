package com.grupo1.editorprocesos.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Wrapper que inyecta el email extraído del JWT como header X-User-Email,
 * manteniendo compatibilidad con los servicios que leen ese header.
 */
public class RequestWithEmail extends HttpServletRequestWrapper {

    private final String email;

    public RequestWithEmail(HttpServletRequest request, String email) {
        super(request);
        this.email = email;
    }

    @Override
    public String getHeader(String name) {
        if ("X-User-Email".equalsIgnoreCase(name)) {
            return email;
        }
        return super.getHeader(name);
    }
}
