package mk.ukim.finki.aibotbackend.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.model.enums.SessionStatus;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;

public record DisplayExtractionSessionDto(
    Long id,
    SocialNetwork socialNetwork,
    SessionStatus status,
    String description,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    List<DisplayExtractionTargetDto> targets
) {
    public static DisplayExtractionSessionDto from(ExtractionSession session) {
        return new DisplayExtractionSessionDto(
            session.getId(),
            session.getSocialNetwork(),
            session.getStatus(),
            session.getDescription(),
            session.getStartedAt(),
            session.getFinishedAt(),
            DisplayExtractionTargetDto.from(session.getTargets())
        );
    }

    public static List<DisplayExtractionSessionDto> from(List<ExtractionSession> sessions) {
        return sessions
            .stream()
            .map(DisplayExtractionSessionDto::from)
            .toList();
    }
}
