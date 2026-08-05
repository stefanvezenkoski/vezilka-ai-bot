package mk.ukim.finki.aibotbackend.service.application;

import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.dto.LoginUserRequestDto;
import mk.ukim.finki.aibotbackend.model.dto.LoginUserResponseDto;
import mk.ukim.finki.aibotbackend.model.dto.RegisterUserRequestDto;
import mk.ukim.finki.aibotbackend.model.dto.RegisterUserResponseDto;

public interface UserApplicationService {
    Optional<RegisterUserResponseDto> register(RegisterUserRequestDto registerUserRequestDto);

    Optional<LoginUserResponseDto> login(LoginUserRequestDto loginUserRequestDto);

    Optional<RegisterUserResponseDto> findByUsername(String username);
}
