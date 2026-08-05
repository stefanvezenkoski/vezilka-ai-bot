package mk.ukim.finki.aibotbackend.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.aibotbackend.model.enums.TargetType;

/**
 * A single thing the bot should visit and extract from,
 * e.g. (PROFILE, "@makedonska.poezija") or (HASHTAG, "#македонски").
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "extraction_targets")
public class ExtractionTarget extends BaseEntity {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetType type;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ExtractionSession session;

    public ExtractionTarget(TargetType type, String value, ExtractionSession session) {
        this.type = type;
        this.value = value;
        this.session = session;
    }
}
