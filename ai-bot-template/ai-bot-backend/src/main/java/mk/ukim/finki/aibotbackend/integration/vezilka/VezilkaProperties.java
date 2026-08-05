package mk.ukim.finki.aibotbackend.integration.vezilka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of the doniraj.vezilka.ai integration,
 * bound from the {@code vezilka.*} properties.
 */
@ConfigurationProperties(prefix = "vezilka")
public record VezilkaProperties(
    String baseUrl,
    String apiKey
) {
}
