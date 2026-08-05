package mk.ukim.finki.aibotbackend.service.application;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.dto.CreateDonationBatchDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayDonationBatchDto;

/**
 * Application service for the donation workflow towards doniraj.vezilka.ai.
 */
public interface DonationApplicationService {
    List<DisplayDonationBatchDto> findAll();

    Optional<DisplayDonationBatchDto> findById(Long id);

    DisplayDonationBatchDto create(CreateDonationBatchDto createDonationBatchDto);

    DisplayDonationBatchDto approve(Long id);

    DisplayDonationBatchDto submit(Long id);
}
