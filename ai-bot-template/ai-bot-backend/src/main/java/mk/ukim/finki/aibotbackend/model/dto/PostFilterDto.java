package mk.ukim.finki.aibotbackend.model.dto;

import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;

/**
 * Optional filters for browsing extracted posts.
 * Any field may be {@code null}, meaning "do not filter by this".
 *
 * @param sessionId               only posts extracted in this session
 * @param socialNetwork           only posts from this network
 * @param minMacedonianConfidence only posts at or above this language confidence
 * @param donated                 {@code true} = only posts already in a donation batch,
 *                                {@code false} = only posts not yet donated
 * @param search                  free-text search over the post content
 */
public record PostFilterDto(
    Long sessionId,
    SocialNetwork socialNetwork,
    Double minMacedonianConfidence,
    Boolean donated,
    String search
) {
}
