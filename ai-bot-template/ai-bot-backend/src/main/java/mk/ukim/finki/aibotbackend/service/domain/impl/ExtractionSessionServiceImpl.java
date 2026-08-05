package mk.ukim.finki.aibotbackend.service.domain.impl;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.repository.ExtractionSessionRepository;
import mk.ukim.finki.aibotbackend.service.domain.ExtractionSessionService;
import org.springframework.stereotype.Service;

@Service
public class ExtractionSessionServiceImpl implements ExtractionSessionService {
    private final ExtractionSessionRepository extractionSessionRepository;

    public ExtractionSessionServiceImpl(ExtractionSessionRepository extractionSessionRepository) {
        this.extractionSessionRepository = extractionSessionRepository;
    }

    @Override
    public List<ExtractionSession> findAll() {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractionSessionService.findAll().");
    }

    @Override
    public Optional<ExtractionSession> findById(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractionSessionService.findById().");
    }

    @Override
    public ExtractionSession create(ExtractionSession session) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractionSessionService.create().");
    }

    @Override
    public ExtractionSession start(Long id) {
        // TODO(student): Validate the current status (only CREATED or PAUSED may
        //  start), set the status to RUNNING, stamp startedAt and save.
        throw new UnsupportedOperationException("TODO(student): Implement ExtractionSessionService.start().");
    }

    @Override
    public ExtractionSession stop(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractionSessionService.stop().");
    }

    @Override
    public ExtractionSession complete(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractionSessionService.complete().");
    }

    @Override
    public ExtractionSession fail(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractionSessionService.fail().");
    }
}
