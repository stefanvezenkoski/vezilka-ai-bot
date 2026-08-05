package mk.ukim.finki.aibotbackend.service.application.impl;

import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.dto.DisplayExtractedPostDto;
import mk.ukim.finki.aibotbackend.model.dto.PostFilterDto;
import mk.ukim.finki.aibotbackend.service.application.ExtractedPostApplicationService;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ExtractedPostApplicationServiceImpl implements ExtractedPostApplicationService {
    private final ExtractedPostService extractedPostService;

    public ExtractedPostApplicationServiceImpl(ExtractedPostService extractedPostService) {
        this.extractedPostService = extractedPostService;
    }

    @Override
    public Page<DisplayExtractedPostDto> findAll(PostFilterDto filter, int page, int size) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractedPostApplicationService.findAll().");
    }

    @Override
    public Optional<DisplayExtractedPostDto> findById(Long id) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractedPostApplicationService.findById().");
    }

    @Override
    public Optional<DisplayExtractedPostDto> deleteById(Long id) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement ExtractedPostApplicationService.deleteById().");
    }
}
