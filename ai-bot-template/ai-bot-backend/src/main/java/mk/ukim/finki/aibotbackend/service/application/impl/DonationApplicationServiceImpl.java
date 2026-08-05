package mk.ukim.finki.aibotbackend.service.application.impl;

import java.util.List;
import java.util.Optional;
import mk.ukim.finki.aibotbackend.model.dto.CreateDonationBatchDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayDonationBatchDto;
import mk.ukim.finki.aibotbackend.service.application.DonationApplicationService;
import mk.ukim.finki.aibotbackend.service.domain.DonationService;
import org.springframework.stereotype.Service;

@Service
public class DonationApplicationServiceImpl implements DonationApplicationService {
    private final DonationService donationService;

    public DonationApplicationServiceImpl(DonationService donationService) {
        this.donationService = donationService;
    }

    @Override
    public List<DisplayDonationBatchDto> findAll() {
        throw new UnsupportedOperationException(
            "TODO(student): Implement DonationApplicationService.findAll().");
    }

    @Override
    public Optional<DisplayDonationBatchDto> findById(Long id) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement DonationApplicationService.findById().");
    }

    @Override
    public DisplayDonationBatchDto create(CreateDonationBatchDto createDonationBatchDto) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement DonationApplicationService.create().");
    }

    @Override
    public DisplayDonationBatchDto approve(Long id) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement DonationApplicationService.approve().");
    }

    @Override
    public DisplayDonationBatchDto submit(Long id) {
        throw new UnsupportedOperationException(
            "TODO(student): Implement DonationApplicationService.submit().");
    }
}
