package mk.ukim.finki.aibotbackend.web.handler;

import mk.ukim.finki.aibotbackend.model.exception.IncorrectPasswordException;
import mk.ukim.finki.aibotbackend.model.exception.UserNotFoundException;
import mk.ukim.finki.aibotbackend.model.exception.UsernameAlreadyExistsException;
import mk.ukim.finki.aibotbackend.web.controller.UserController;
import mk.ukim.finki.aibotbackend.web.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerExceptionHandler {
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyExists(UsernameAlreadyExistsException exception) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiError.of(HttpStatus.CONFLICT, exception.getMessage()));
    }

    @ExceptionHandler({UserNotFoundException.class, IncorrectPasswordException.class})
    public ResponseEntity<ApiError> handleInvalidCredentials(RuntimeException exception) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(HttpStatus.UNAUTHORIZED, exception.getMessage()));
    }
}
