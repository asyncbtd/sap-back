package io.github.asyncbtd.sap.core.exception;

import org.springframework.http.HttpStatus;

public class InternalErrorHttpException extends AbstractHttpException {

    public InternalErrorHttpException(String message) {
      super(message);
    }

    public InternalErrorHttpException(String message, Throwable cause) {
      super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
