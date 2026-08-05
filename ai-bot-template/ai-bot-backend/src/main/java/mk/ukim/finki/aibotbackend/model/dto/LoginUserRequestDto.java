package mk.ukim.finki.aibotbackend.model.dto;

public record LoginUserRequestDto(
    String username,
    String password
) {
}
