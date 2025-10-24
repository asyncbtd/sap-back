package io.github.asyncbtd.sap.core.exception;

import org.springframework.http.HttpStatus;

public abstract class AbstractHttpException extends RuntimeException {

    public AbstractHttpException(String message) {
        super(message);
    }

    public AbstractHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract HttpStatus getHttpStatus();
}
