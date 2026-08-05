package mk.ukim.finki.aibotbackend.bot.core;

import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.model.exception.SessionNotFoundException;
import mk.ukim.finki.aibotbackend.service.domain.BotActionLogService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractionSessionService;
import org.springframework.stereotype.Service;

@Service
public class BotOrchestratorImpl implements BotOrchestrator {
    private final SocialNetworkBot socialNetworkBot;
    private final ExtractionSessionService extractionSessionService;
    private final ExtractedPostService extractedPostService;
    private final BotActionLogService botActionLogService;

    public BotOrchestratorImpl(
        SocialNetworkBot socialNetworkBot,
        ExtractionSessionService extractionSessionService,
        ExtractedPostService extractedPostService,
        BotActionLogService botActionLogService
    ) {
        this.socialNetworkBot = socialNetworkBot;
        this.extractionSessionService = extractionSessionService;
        this.extractedPostService = extractedPostService;
        this.botActionLogService = botActionLogService;
    }

    @Override
    public void runSession(Long sessionId) {
        ExtractionSession session = extractionSessionService
            .findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // TODO(student): Orchestrate the full run:
        //  1. socialNetworkBot.login()
        //  2. for each target of the session:
        //       socialNetworkBot.execute(target,
        //           (action, successful) -> botActionLogService.log(session, action, successful))
        //     then map the returned DTOs with CreateExtractedPostDto.toExtractedPost(session)
        //     and persist them with extractedPostService.saveAll(...)
        //  3. mark the session COMPLETED via extractionSessionService.complete(sessionId),
        //     or FAILED via extractionSessionService.fail(sessionId) when something goes wrong
        //  4. always socialNetworkBot.shutdown() at the end
        throw new UnsupportedOperationException("TODO(student): Implement BotOrchestrator.runSession().");
    }
}
