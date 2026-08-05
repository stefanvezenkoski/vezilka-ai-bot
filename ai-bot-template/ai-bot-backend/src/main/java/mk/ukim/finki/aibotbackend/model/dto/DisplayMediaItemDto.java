package mk.ukim.finki.aibotbackend.model.dto;

import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.MediaItem;
import mk.ukim.finki.aibotbackend.model.enums.MediaType;

public record DisplayMediaItemDto(
    Long id,
    MediaType type,
    String sourceUrl,
    String storagePath
) {
    public static DisplayMediaItemDto from(MediaItem mediaItem) {
        return new DisplayMediaItemDto(
            mediaItem.getId(),
            mediaItem.getType(),
            mediaItem.getSourceUrl(),
            mediaItem.getStoragePath()
        );
    }

    public static List<DisplayMediaItemDto> from(List<MediaItem> mediaItems) {
        return mediaItems
            .stream()
            .map(DisplayMediaItemDto::from)
            .toList();
    }
}
