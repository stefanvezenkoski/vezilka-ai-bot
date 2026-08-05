package mk.ukim.finki.aibotbackend.service.application;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractionSessionDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayBotActionLogDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayExtractionSessionDto;

/**
 * Application service for extraction sessions: orchestrates the domain
 * services and maps between entities and DTOs.
 */
public interface ExtractionSessionApplicationService {
    List<DisplayExtractionSessionDto> findAll();

    Optional<DisplayExtractionSessionDto> findById(Long id);

    DisplayExtractionSessionDto create(CreateExtractionSessionDto createExtractionSessionDto);

    /**
     * Starts the session and publishes a {@code SessionStartedEvent} so the
     * bot run happens asynchronously, outside of the web request.
     */
    DisplayExtractionSessionDto start(Long id);

    DisplayExtractionSessionDto stop(Long id);

    List<DisplayBotActionLogDto> findLogsBySessionId(Long id);
}
