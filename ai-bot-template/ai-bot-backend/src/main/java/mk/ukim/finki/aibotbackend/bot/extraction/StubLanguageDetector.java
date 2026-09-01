package mk.ukim.finki.aibotbackend.bot.extraction;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detector that evaluates Macedonian language confidence for extracted text.
 */
@Component
public class StubLanguageDetector implements LanguageDetector {

    private static final Pattern CYRILLIC_PATTERN = Pattern.compile("[а-шА-ШЈјЃѓЌќЅѕЏџ]");
    private static final Set<String> MACEDONIAN_STOP_WORDS = Set.of(
            "во", "со", "на", "за", "од", "не", "да", "ќе", "е", "се", "ги", "му", "ѝ", "сме", "сте", "секој", "биде", "биди"
    );

    @Override
    public double macedonianConfidence(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }

        String trimmed = text.trim();
        int totalChars = trimmed.length();
        
        // Count Cyrillic characters
        Matcher matcher = CYRILLIC_PATTERN.matcher(trimmed);
        int cyrillicCount = 0;
        while (matcher.find()) {
            cyrillicCount++;
        }

        double cyrillicRatio = (double) cyrillicCount / totalChars;

        // Check for specific Macedonian stop words
        String[] words = trimmed.toLowerCase().split("\\s+");
        int matchCount = 0;
        for (String word : words) {
            String cleanWord = word.replaceAll("[^а-шјѓќѕџ]", "");
            if (MACEDONIAN_STOP_WORDS.contains(cleanWord)) {
                matchCount++;
            }
        }

        double stopWordBonus = Math.min(1.0, (double) matchCount / Math.max(1, words.length / 5.0));

        // Combine ratios: if high Cyrillic content and presence of Macedonian words -> high score
        if (cyrillicRatio > 0.05) {
            return Math.min(1.0, cyrillicRatio * 0.6 + stopWordBonus * 0.4);
        }

        return 0.0;
    }
}
