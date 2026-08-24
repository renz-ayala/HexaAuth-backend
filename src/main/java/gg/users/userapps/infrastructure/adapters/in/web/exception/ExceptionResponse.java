package gg.users.userapps.infrastructure.adapters.in.web.exception;

public record ExceptionResponse(
        String error,
        String detalle
) {}
