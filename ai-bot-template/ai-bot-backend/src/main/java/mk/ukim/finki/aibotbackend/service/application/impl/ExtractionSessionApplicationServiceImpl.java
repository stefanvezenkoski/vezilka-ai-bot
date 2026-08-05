package mk.ukim.finki.aibotbackend.service.application.impl;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractionSessionDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayBotActionLogDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayExtractionSessionDto;
import mk.ukim.finki.aibotbackend.service.application.ExtractionSessionApplicationService;
import mk.ukim.finki.aibotbackend.service.domain.BotActionLogService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractionSessionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ExtractionSessionApplicationServiceImpl implements ExtractionSessionApplicationService {
    private final ExtractionSessionService extractionSessionService;
    private final BotActionLogService botActionLogService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ExtractionSessionApplicationServiceImpl(
        ExtractionSessionService extractionSessionService,
        BotActionLogService botActionLogService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.extractionSessionService = extractionSessionService;
        this.botActionLogService = botActionLogService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public List<DisplayExtractionSessionDto> findAll() {
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractionSessionApplicationService.findAll().");
    }

    @Override
    public Optional<DisplayExtractionSessionDto> findById(Long id) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractionSessionApplicationService.findById().");
    }

    @Override
    public DisplayExtractionSessionDto create(CreateExtractionSessionDto createExtractionSessionDto) {
        // TODO(student): Map the DTO to an entity (toExtractionSession), delegate to
        //  the domain service and map the result back (DisplayExtractionSessionDto.from).
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractionSessionApplicationService.create().");
    }

    @Override
    public DisplayExtractionSessionDto start(Long id) {
        // TODO(student): Start the session via the domain service, then publish
        //  new SessionStartedEvent(id) with applicationEventPublisher — the
        //  SessionStartedListener picks it up and runs the bot asynchronously.
        //  This method needs to run in a transaction for the AFTER_COMMIT
        //  listener to fire (see jakarta.transaction.Transactional).
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractionSessionApplicationService.start().");
    }

    @Override
    public DisplayExtractionSessionDto stop(Long id) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractionSessionApplicationService.stop().");
    }

    @Override
    public List<DisplayBotActionLogDto> findLogsBySessionId(Long id) {
        return DisplayBotActionLogDto.from(botActionLogService.findBySessionId(id));
    }
}
