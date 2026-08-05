package mk.ukim.finki.aibotbackend.service.domain;

import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    Optional<User> findByUsername(String username);

    User register(User user);

    User login(String username, String password);
}
