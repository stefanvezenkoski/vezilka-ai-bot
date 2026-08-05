package mk.ukim.finki.aibotbackend.model.dto;

import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionTarget;
import mk.ukim.finki.aibotbackend.model.enums.TargetType;

public record DisplayExtractionTargetDto(
    Long id,
    TargetType type,
    String value
) {
    public static DisplayExtractionTargetDto from(ExtractionTarget target) {
        return new DisplayExtractionTargetDto(
            target.getId(),
            target.getType(),
            target.getValue()
        );
    }

    public static List<DisplayExtractionTargetDto> from(List<ExtractionTarget> targets) {
        return targets
            .stream()
            .map(DisplayExtractionTargetDto::from)
            .toList();
    }
}
