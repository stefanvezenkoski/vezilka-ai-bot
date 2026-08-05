package mk.ukim.finki.aibotbackend.repository;

import java.util.List;
import mk.ukim.finki.aibotbackend.model.domain.BotActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotActionLogRepository extends JpaRepository<BotActionLog, Long> {
    List<BotActionLog> findAllBySessionIdOrderByOccurredAtAsc(Long sessionId);
}
