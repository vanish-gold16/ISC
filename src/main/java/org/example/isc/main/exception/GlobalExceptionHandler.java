package org.example.isc.main.exception;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> generalException(Exception exception) {
        logger.error(exception.getMessage(), exception);
        var responseDTO = new ErrorResponseDTO(
                "Internal Server Error",
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> notFoundException(Exception exception) {
        logger.error(exception.getMessage(), exception);
        var responseDTO = new ErrorResponseDTO(
                "Entity Not Found",
                exception.getMessage(),
                LocalDateTime.now()
        );

        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTO);
    }

    @ExceptionHandler(exception = {
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ErrorResponseDTO> illegalIdentifierException(Exception exception) {
        logger.error(exception.getMessage(), exception);

        var responseDTO = new ErrorResponseDTO(
                "Bad Request",
                exception.getMessage(),
                LocalDateTime.now()
        );

        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
    }

}
