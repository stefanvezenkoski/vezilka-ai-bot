package mk.ukim.finki.aibotbackend.bot.core;

import java.util.ArrayList;
import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.ExtractedPost;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionSession;
import mk.ukim.finki.aibotbackend.model.domain.ExtractionTarget;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractedPostDto;
import mk.ukim.finki.aibotbackend.model.exception.SessionNotFoundException;
import mk.ukim.finki.aibotbackend.service.domain.BotActionLogService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractionSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BotOrchestratorImpl implements BotOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(BotOrchestratorImpl.class);

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
        log.info("Starting orchestration for session ID: {}", sessionId);
        ExtractionSession session = extractionSessionService
            .findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        try {
            // 1. Mark session RUNNING
            extractionSessionService.start(sessionId);

            // 2. Perform initial login / site startup
            socialNetworkBot.login();

            // 3. Process each target in the session
            for (ExtractionTarget target : session.getTargets()) {
                log.info("Executing target {} for session {}", target.getValue(), sessionId);
                
                List<CreateExtractedPostDto> extractedDtos = socialNetworkBot.execute(
                    target,
                    (action, successful) -> botActionLogService.log(session, action, successful)
                );

                List<ExtractedPost> postsToSave = new ArrayList<>();
                for (CreateExtractedPostDto dto : extractedDtos) {
                    ExtractedPost post = dto.toExtractedPost(session);
                    postsToSave.add(post);
                }

                if (!postsToSave.isEmpty()) {
                    extractedPostService.saveAll(postsToSave);
                    log.info("Saved {} extracted posts for target {}", postsToSave.size(), target.getValue());
                }
            }

            // 4. Mark session COMPLETED
            extractionSessionService.complete(sessionId);
            log.info("Successfully completed session ID: {}", sessionId);

        } catch (Exception e) {
            log.error("Session failed during orchestration", e);
            try {
                extractionSessionService.fail(sessionId);
            } catch (Exception ex) {
                log.error("Failed to mark session as FAILED", ex);
            }
        } finally {
            // 5. Always shutdown browser resources at the end
            try {
                socialNetworkBot.shutdown();
            } catch (Exception e) {
                log.error("Error shutting down social network bot", e);
            }
        }
    }
}
