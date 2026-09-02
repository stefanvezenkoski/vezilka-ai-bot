package mk.ukim.finki.aibotbackend.service.domain.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.persistence.criteria.Predicate;
import mk.ukim.finki.aibotbackend.model.domain.ExtractedPost;
import mk.ukim.finki.aibotbackend.model.dto.PostFilterDto;
import mk.ukim.finki.aibotbackend.repository.ExtractedPostRepository;
import mk.ukim.finki.aibotbackend.service.domain.ExtractedPostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ExtractedPostServiceImpl implements ExtractedPostService {

    private static final Logger log = LoggerFactory.getLogger(ExtractedPostServiceImpl.class);

    private final ExtractedPostRepository extractedPostRepository;

    public ExtractedPostServiceImpl(ExtractedPostRepository extractedPostRepository) {
        this.extractedPostRepository = extractedPostRepository;
    }

    @Override
    public Page<ExtractedPost> findAll(PostFilterDto filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Specification<ExtractedPost> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.sessionId() != null) {
                    predicates.add(cb.equal(root.get("session").get("id"), filter.sessionId()));
                }
                if (filter.socialNetwork() != null) {
                    predicates.add(cb.equal(root.get("session").get("socialNetwork"), filter.socialNetwork()));
                }
                if (filter.minMacedonianConfidence() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("macedonianConfidence"), filter.minMacedonianConfidence()));
                }
                if (filter.donated() != null) {
                    if (filter.donated()) {
                        predicates.add(cb.isNotNull(root.get("donationBatch")));
                    } else {
                        predicates.add(cb.isNull(root.get("donationBatch")));
                    }
                }
                if (filter.search() != null && !filter.search().isBlank()) {
                    String pattern = "%" + filter.search().toLowerCase() + "%";
                    Predicate contentLike = cb.like(cb.lower(root.get("content")), pattern);
                    Predicate authorLike = cb.like(cb.lower(root.get("authorHandle")), pattern);
                    Predicate urlLike = cb.like(cb.lower(root.get("sourceUrl")), pattern);
                    predicates.add(cb.or(contentLike, authorLike, urlLike));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return extractedPostRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<ExtractedPost> findById(Long id) {
        return extractedPostRepository.findById(id);
    }

    @Override
    public List<ExtractedPost> findAllById(List<Long> ids) {
        return extractedPostRepository.findAllById(ids);
    }

    @Override
    public List<ExtractedPost> saveAll(List<ExtractedPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        List<ExtractedPost> uniqueToSave = new ArrayList<>();
        Set<String> seenContentInBatch = new HashSet<>();

        for (ExtractedPost post : posts) {
            String content = post.getContent() != null ? post.getContent().trim() : "";
            if (content.isBlank()) {
                continue;
            }

            Long sessionId = post.getSession() != null ? post.getSession().getId() : null;

            boolean existsInSession = sessionId != null &&
                    extractedPostRepository.existsBySessionIdAndContent(sessionId, content);

            if (!seenContentInBatch.contains(content) && !existsInSession) {
                seenContentInBatch.add(content);
                uniqueToSave.add(post);
            }
        }

        if (uniqueToSave.isEmpty()) {
            return List.of();
        }

        log.info("Saving {} new extracted posts to database...", uniqueToSave.size());
        return extractedPostRepository.saveAll(uniqueToSave);
    }

    @Override
    public Optional<ExtractedPost> deleteById(Long id) {
        Optional<ExtractedPost> postOpt = extractedPostRepository.findById(id);
        if (postOpt.isPresent()) {
            extractedPostRepository.deleteById(id);
        }
        return postOpt;
    }
}
