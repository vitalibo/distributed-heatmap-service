package com.github.vitalibo.heatmap.api.infrastructure.springframework;

import com.github.vitalibo.heatmap.api.core.model.HttpError;
import com.github.vitalibo.heatmap.api.core.util.ErrorState;
import com.github.vitalibo.heatmap.api.core.util.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestControllerAdvice
public final class ControllerExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<HttpError> handleInternalServerError(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            .withRequestId(response.getHeader("Request-Id"));
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ResponseEntity<HttpError> handleNotFound(HttpServletRequest request, HttpServletResponse response, NoHandlerFoundException ex) {
        return new ErrorResponseEntity(HttpStatus.NOT_FOUND)
            .withRequestId(response.getHeader("Request-Id"));
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpError> handleMethodNotAllowed(HttpServletRequest request, HttpServletResponse response, HttpRequestMethodNotSupportedException ex) {
        return new ErrorResponseEntity(HttpStatus.NOT_FOUND)
            .withRequestId(response.getHeader("Request-Id"));
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<HttpError> handleBadRequest(HttpServletRequest request, HttpServletResponse response, ValidationException ex) {
        return new ErrorResponseEntity(HttpStatus.BAD_REQUEST)
            .withRequestId(response.getHeader("Request-Id"))
            .withErrorState(ex.getErrorState());
    }

    private static class ErrorResponseEntity extends ResponseEntity<HttpError> {

        public ErrorResponseEntity(HttpStatus status) {
            this(new HttpError(status.value(), status.getReasonPhrase(), null, null), status);
        }

        private ErrorResponseEntity(HttpError body, HttpStatus status) {
            super(body, status);
        }

        public ErrorResponseEntity withErrorState(ErrorState errorState) {
            return new ErrorResponseEntity(
                this.getBody()
                    .withErrors(errorState),
                this.getStatusCode());
        }

        public ErrorResponseEntity withRequestId(String requestId) {
            return new ErrorResponseEntity(
                this.getBody()
                    .withRequestId(requestId),
                this.getStatusCode());
        }

    }

}
