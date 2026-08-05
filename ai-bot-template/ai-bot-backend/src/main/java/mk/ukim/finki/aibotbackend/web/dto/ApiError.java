package mk.ukim.finki.aibotbackend.web.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ApiError(
    Integer status,
    String message,
    LocalDateTime timestamp
) {
    public static ApiError of(HttpStatus status, String message) {
        return new ApiError(status.value(), message, LocalDateTime.now());
    }
}
