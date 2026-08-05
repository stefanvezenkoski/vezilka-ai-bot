package mk.ukim.finki.aibotbackend.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import mk.ukim.finki.aibotbackend.model.dto.CreateExtractionSessionDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayBotActionLogDto;
import mk.ukim.finki.aibotbackend.model.dto.DisplayExtractionSessionDto;
import mk.ukim.finki.aibotbackend.service.application.ExtractionSessionApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The bot control panel API: create extraction sessions, start/stop them and
 * follow what the bot did.
 */
@RestController
@RequestMapping("/api/sessions")
public class ExtractionSessionController {
    private final ExtractionSessionApplicationService extractionSessionApplicationService;

    public ExtractionSessionController(ExtractionSessionApplicationService extractionSessionApplicationService) {
        this.extractionSessionApplicationService = extractionSessionApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<DisplayExtractionSessionDto>> findAll() {
        return ResponseEntity.ok(extractionSessionApplicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisplayExtractionSessionDto> findById(@PathVariable Long id) {
        return extractionSessionApplicationService
            .findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<DisplayExtractionSessionDto> create(
        @RequestBody @Valid CreateExtractionSessionDto createExtractionSessionDto
    ) {
        return ResponseEntity.ok(extractionSessionApplicationService.create(createExtractionSessionDto));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<DisplayExtractionSessionDto> start(@PathVariable Long id) {
        return ResponseEntity.ok(extractionSessionApplicationService.start(id));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<DisplayExtractionSessionDto> stop(@PathVariable Long id) {
        return ResponseEntity.ok(extractionSessionApplicationService.stop(id));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<DisplayBotActionLogDto>> findLogs(@PathVariable Long id) {
        return ResponseEntity.ok(extractionSessionApplicationService.findLogsBySessionId(id));
    }
}
