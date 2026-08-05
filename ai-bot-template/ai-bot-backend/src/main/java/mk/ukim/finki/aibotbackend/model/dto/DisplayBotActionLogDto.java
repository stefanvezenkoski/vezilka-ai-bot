package mk.ukim.finki.aibotbackend.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.BotActionLog;
import mk.ukim.finki.aibotbackend.model.enums.BotActionType;

public record DisplayBotActionLogDto(
    Long id,
    BotActionType actionType,
    String details,
    Boolean successful,
    LocalDateTime occurredAt
) {
    public static DisplayBotActionLogDto from(BotActionLog log) {
        return new DisplayBotActionLogDto(
            log.getId(),
            log.getActionType(),
            log.getDetails(),
            log.getSuccessful(),
            log.getOccurredAt()
        );
    }

    public static List<DisplayBotActionLogDto> from(List<BotActionLog> logs) {
        return logs
            .stream()
            .map(DisplayBotActionLogDto::from)
            .toList();
    }
}
