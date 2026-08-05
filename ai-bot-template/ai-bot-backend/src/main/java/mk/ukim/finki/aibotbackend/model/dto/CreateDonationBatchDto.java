package mk.ukim.finki.aibotbackend.model.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request for grouping already-extracted posts into a donation batch.
 */
public record CreateDonationBatchDto(
    @NotEmpty
    List<Long> postIds
) {
}
