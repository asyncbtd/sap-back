package io.github.asyncbtd.sap.core;

import io.github.asyncbtd.sap.core.exception.AbstractHttpException;
import io.github.asyncbtd.sap.web.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AbstractHttpException.class)
    public ResponseEntity<ErrorResponse> handleHttpException(AbstractHttpException ex) {
        if (ex.getHttpStatus() == INTERNAL_SERVER_ERROR) {
            log.error("{}", ex.getClass().getName(), ex);
        }
        return ResponseEntity.status(ex.getHttpStatus()).body(
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        var errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .message(errorMessage)
                        .build());
    }
}
