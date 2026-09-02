package mk.ukim.finki.aibotbackend.repository;

import mk.ukim.finki.aibotbackend.model.domain.ExtractedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractedPostRepository
    extends JpaRepository<ExtractedPost, Long>, JpaSpecificationExecutor<ExtractedPost> {

    boolean existsBySessionIdAndSourceUrl(Long sessionId, String sourceUrl);

    boolean existsBySessionIdAndContent(Long sessionId, String content);

    boolean existsByContent(String content);
}
