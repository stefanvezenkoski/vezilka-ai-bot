package mk.ukim.finki.aibotbackend.bot.core;

import mk.ukim.finki.aibotbackend.bot.llm.BotAction;

/**
 * Callback invoked by the agentic loop after every performed action, so the
 * caller (the {@code BotOrchestrator}) can persist a {@code BotActionLog}
 * without the bot layer depending on services or repositories.
 */
@FunctionalInterface
public interface BotStepListener {
    void onStep(BotAction action, boolean successful);
}
