package pl.edu.pk.accelapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> illegal(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> grammar(BadSqlGrammarException e) {
        return Map.of("error", "Błąd składni SQL", "detail", e.getSQLException().getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String,Object> other(Exception e) {
        e.printStackTrace(); // tymczasowo, albo logger.error("...", e)
        return Map.of("error","Błąd serwera");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Map<String,Object> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return Map.of("error","Method Not Allowed","allowed", e.getSupportedHttpMethods());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> badBody(Exception e) {
        return Map.of("error","Nieprawidłowe body JSON");
    }
}

