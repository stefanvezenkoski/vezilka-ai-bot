package mk.ukim.finki.aibotbackend.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.aibotbackend.model.enums.BotActionType;

/**
 * One step of the agentic loop, persisted so a session's behaviour can be
 * traced and shown live in the frontend.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bot_action_logs")
public class BotActionLog extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ExtractionSession session;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BotActionType actionType;

    @Column(columnDefinition = "text")
    private String details;

    @Column(nullable = false)
    private Boolean successful;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    public BotActionLog(
        ExtractionSession session,
        BotActionType actionType,
        String details,
        Boolean successful,
        LocalDateTime occurredAt
    ) {
        this.session = session;
        this.actionType = actionType;
        this.details = details;
        this.successful = successful;
        this.occurredAt = occurredAt;
    }
}
