package io.github.asyncbtd.sap.core.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedHttpException extends AbstractHttpException {

    public UnauthorizedHttpException(String message) {
        super(message);
    }

    public UnauthorizedHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }
}
