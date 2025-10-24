package io.github.asyncbtd.sap.core.exception;

import org.springframework.http.HttpStatus;

public class ConflictHttpException extends AbstractHttpException {

    public ConflictHttpException(String message) {
        super(message);
    }

    public ConflictHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
