package gg.users.userapps.infrastructure.exception;

import gg.users.userapps.infrastructure.exception.object.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ExceptionResponse>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        var errors = new ArrayList<ExceptionResponse>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            var err = new ExceptionResponse(
                    error.getField(),
                    "inválid input"
            );

            errors.add(err);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(IllegalArgumentException ex) {
        return this.buildResponse("Validación fallida", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException ex) {
        return this.buildResponse("Error en la operación", ex.getMessage(), HttpStatus.CONFLICT);
    }

    private ResponseEntity<ExceptionResponse> buildResponse(String error, String detalle, HttpStatus status) {
        return ResponseEntity.status(status).body(
                new ExceptionResponse(error, detalle)
        );
    }
}