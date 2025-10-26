package io.github.asyncbtd.sap.core.exception;

import org.springframework.http.HttpStatus;

public class BadRequestHttpException extends AbstractHttpException {

    public BadRequestHttpException(String message) {
        super(message);
    }

    public BadRequestHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
