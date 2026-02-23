package org.example.isc.main.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(Exception.class)
    public ModelAndView generalException(Exception exception, HttpServletRequest request) {
        logger.error(exception.getMessage(), exception);
        return buildErrorView(HttpStatus.INTERNAL_SERVER_ERROR, exception, request, "error/500");
    }

    @ExceptionHandler({
            EntityNotFoundException.class,
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ModelAndView notFoundException(Exception exception, HttpServletRequest request) {
        logger.error(exception.getMessage(), exception);
        return buildErrorView(HttpStatus.NOT_FOUND, exception, request, "error/404");
    }

    @ExceptionHandler(exception = {
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ModelAndView illegalIdentifierException(Exception exception, HttpServletRequest request) {
        logger.error(exception.getMessage(), exception);
        return buildErrorView(HttpStatus.BAD_REQUEST, exception, request, "error/400");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView responseStatusException(ResponseStatusException exception, HttpServletRequest request) {
        logger.error(exception.getMessage(), exception);
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String viewName = status == HttpStatus.NOT_FOUND ? "error/404" : "error/500";
        return buildErrorView(status, exception, request, viewName);
    }

    private ModelAndView buildErrorView(
            HttpStatus status,
            Exception exception,
            HttpServletRequest request,
            String viewName
    ) {
        var modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", status.getReasonPhrase());
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }
}
