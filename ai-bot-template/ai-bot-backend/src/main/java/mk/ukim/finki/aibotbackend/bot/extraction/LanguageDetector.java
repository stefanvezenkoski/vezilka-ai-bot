package mk.ukim.finki.aibotbackend.bot.extraction;

/**
 * Detects whether a text is written in Macedonian — the whole point of the
 * project is to donate <i>Macedonian</i> content to doniraj.vezilka.ai.
 *
 * <p>TODO(student): Provide an implementation. Options include a language
 * detection library, an {@code LlmClient} prompt, or a heuristic over the
 * Cyrillic script combined with Macedonian-specific letters (ѓ, ќ, ѕ, џ, љ, њ).</p>
 */
public interface LanguageDetector {
    /**
     * @return confidence in the range [0.0, 1.0] that {@code text} is Macedonian
     */
    double macedonianConfidence(String text);
}
