package mk.ukim.finki.aibotbackend.service.application;

import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.dto.DisplayExtractedPostDto;
import mk.ukim.finki.aibotbackend.model.dto.PostFilterDto;
import org.springframework.data.domain.Page;

/**
 * Application service for browsing the extracted content.
 */
public interface ExtractedPostApplicationService {
    Page<DisplayExtractedPostDto> findAll(PostFilterDto filter, int page, int size);

    Optional<DisplayExtractedPostDto> findById(Long id);

    Optional<DisplayExtractedPostDto> deleteById(Long id);
}
