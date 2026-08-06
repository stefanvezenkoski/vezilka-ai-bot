package mk.ukim.finki.aibotbackend.service.application.impl;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.events.SessionStartedEvent;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractionSessionDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayBotActionLogDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayExtractionSessionDto;
import mk.ukim.finki.aibotbackend.service.application.ExtractionSessionApplicationService;
import mk.ukim.finki.aibotbackend.service.domain.BotActionLogService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractionSessionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return DisplayExtractionSessionDto.from(extractionSessionService.findAll());
    }

    @Override
    public Optional<DisplayExtractionSessionDto> findById(Long id) {
        return extractionSessionService.findById(id)
                .map(DisplayExtractionSessionDto::from);
    }

    @Override
    public DisplayExtractionSessionDto create(CreateExtractionSessionDto createExtractionSessionDto) {
        ExtractionSession session = createExtractionSessionDto.toExtractionSession();
        ExtractionSession created = extractionSessionService.create(session);
        return DisplayExtractionSessionDto.from(created);
    }

    @Override
    @Transactional
    public DisplayExtractionSessionDto start(Long id) {
        ExtractionSession startedSession = extractionSessionService.start(id);
        applicationEventPublisher.publishEvent(new SessionStartedEvent(id));
        return DisplayExtractionSessionDto.from(startedSession);
    }

    @Override
    @Transactional
    public DisplayExtractionSessionDto stop(Long id) {
        ExtractionSession stoppedSession = extractionSessionService.stop(id);
        return DisplayExtractionSessionDto.from(stoppedSession);
    }

    @Override
    public List<DisplayBotActionLogDto> findLogsBySessionId(Long id) {
        return DisplayBotActionLogDto.from(botActionLogService.findBySessionId(id));
    }
}
