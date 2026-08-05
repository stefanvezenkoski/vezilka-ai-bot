package mk.ukim.finki.aibotbackend.service.domain.impl;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.domain.ExtractedPost;
import mk.ukim.finki.aibotbackend.model.dto.PostFilterDto;
import mk.ukim.finki.aibotbackend.repository.ExtractedPostRepository;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ExtractedPostServiceImpl implements ExtractedPostService {
    private final ExtractedPostRepository extractedPostRepository;

    public ExtractedPostServiceImpl(ExtractedPostRepository extractedPostRepository) {
        this.extractedPostRepository = extractedPostRepository;
    }

    @Override
    public Page<ExtractedPost> findAll(PostFilterDto filter, int page, int size) {
        // TODO(student): Combine the non-null filter fields into a query, e.g.
        //  with JPA Specifications (ExtractedPostRepository already extends
        //  JpaSpecificationExecutor).
        throw new UnsupportedOperationException("TODO(student): Implement ExtractedPostService.findAll().");
    }

    @Override
    public Optional<ExtractedPost> findById(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractedPostService.findById().");
    }

    @Override
    public List<ExtractedPost> findAllById(List<Long> ids) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractedPostService.findAllById().");
    }

    @Override
    public List<ExtractedPost> saveAll(List<ExtractedPost> posts) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractedPostService.saveAll().");
    }

    @Override
    public Optional<ExtractedPost> deleteById(Long id) {
        throw new UnsupportedOperationException("TODO(student): Implement ExtractedPostService.deleteById().");
    }
}
