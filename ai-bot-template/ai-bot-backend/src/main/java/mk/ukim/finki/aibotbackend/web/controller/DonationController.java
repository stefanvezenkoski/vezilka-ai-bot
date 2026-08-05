package mk.ukim.finki.aibotbackend.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import mk.ukim.finki.aibotbackend.model.dto.CreateDonationBatchDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayDonationBatchDto;
import mk.ukim.finki.aibotbackend.service.application.DonationApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The donation dashboard API: group extracted posts into batches, review and
 * approve them, and submit them to doniraj.vezilka.ai.
 */
@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationApplicationService donationApplicationService;

    public DonationController(DonationApplicationService donationApplicationService) {
        this.donationApplicationService = donationApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<DisplayDonationBatchDto>> findAll() {
        return ResponseEntity.ok(donationApplicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisplayDonationBatchDto> findById(@PathVariable Long id) {
        return donationApplicationService
            .findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<DisplayDonationBatchDto> create(
        @RequestBody @Valid CreateDonationBatchDto createDonationBatchDto
    ) {
        return ResponseEntity.ok(donationApplicationService.create(createDonationBatchDto));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<DisplayDonationBatchDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(donationApplicationService.approve(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<DisplayDonationBatchDto> submit(@PathVariable Long id) {
        return ResponseEntity.ok(donationApplicationService.submit(id));
    }
}
