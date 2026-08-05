package mk.ukim.finki.aibotbackend.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;

public record CreateExtractionSessionDto(
    @NotNull
    SocialNetwork socialNetwork,
    String description,
    @NotEmpty
    List<@Valid CreateExtractionTargetDto> targets
) {
    public ExtractionSession toExtractionSession() {
        ExtractionSession session = new ExtractionSession(socialNetwork, description);
        targets
            .stream()
            .map(target -> target.toExtractionTarget(session))
            .forEach(session.getTargets()::add);
        return session;
    }
}
