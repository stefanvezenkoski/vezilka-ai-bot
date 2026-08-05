package mk.ukim.finki.aibotbackend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mk.ukim.finki.aibotbackend.model.domain.ExtractedPost;
import mk.ukim.finki.aibotbackend.model.domain.MediaItem;
import mk.ukim.finki.aibotbackend.model.enums.MediaType;

public record CreateMediaItemDto(
    @NotNull
    MediaType type,
    @NotBlank
    String sourceUrl,
    String storagePath
) {
    public MediaItem toMediaItem(ExtractedPost post) {
        return new MediaItem(post, type, sourceUrl, storagePath);
    }
}
