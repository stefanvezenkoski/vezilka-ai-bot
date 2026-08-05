package mk.ukim.finki.aibotbackend.service.domain.impl;

import java.time.LocalDateTime;
import java.util.List;
import mk.ukim.finki.aibotbackend.bot.llm.BotAction;
import mk.ukim.finki.aibotbackend.model.domain.BotActionLog;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.repository.BotActionLogRepository;
import mk.ukim.finki.aibotbackend.service.domain.BotActionLogService;
import org.springframework.stereotype.Service;

@Service
public class BotActionLogServiceImpl implements BotActionLogService {
    private final BotActionLogRepository botActionLogRepository;

    public BotActionLogServiceImpl(BotActionLogRepository botActionLogRepository) {
        this.botActionLogRepository = botActionLogRepository;
    }

    @Override
    public BotActionLog log(ExtractionSession session, BotAction action, boolean successful) {
        String details = action.target() == null
            ? action.reasoning()
            : "%s — %s".formatted(action.target(), action.reasoning());
        return botActionLogRepository.save(new BotActionLog(
            session,
            action.type(),
            details,
            successful,
            LocalDateTime.now()
        ));
    }

    @Override
    public List<BotActionLog> findBySessionId(Long sessionId) {
        return botActionLogRepository.findAllBySessionIdOrderByOccurredAtAsc(sessionId);
    }
}
