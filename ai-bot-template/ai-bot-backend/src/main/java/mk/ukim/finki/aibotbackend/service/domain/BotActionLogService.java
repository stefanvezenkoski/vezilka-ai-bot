package mk.ukim.finki.aibotbackend.service.domain;

import java.util.List;
import mk.ukim.finki.aibotbackend.bot.llm.BotAction;
import mk.ukim.finki.aibotbackend.model.domain.BotActionLog;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;

/**
 * Persists and reads the trace of the agentic loop.
 * Fully provided — this is infrastructure for observing your bot, not part
 * of the assignment.
 */
public interface BotActionLogService {
    BotActionLog log(ExtractionSession session, BotAction action, boolean successful);

    List<BotActionLog> findBySessionId(Long sessionId);
}
