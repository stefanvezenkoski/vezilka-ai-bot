package mk.ukim.finki.aibotbackend.web.controller;

import mk.ukim.finki.aibotbackend.model.dto.DisplayExtractedPostDto;
import mk.ukim.finki.aibotbackend.model.dto.PostFilterDto;
import mk.ukim.finki.aibotbackend.model.enums.SocialNetwork;
import mk.ukim.finki.aibotbackend.service.application.ExtractedPostApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The extracted-content browser API: paged, filterable access to everything
 * the bot has collected.
 */
@RestController
@RequestMapping("/api/posts")
public class ExtractedPostController {
    private final ExtractedPostApplicationService extractedPostApplicationService;

    public ExtractedPostController(ExtractedPostApplicationService extractedPostApplicationService) {
        this.extractedPostApplicationService = extractedPostApplicationService;
    }

    @GetMapping
    public ResponseEntity<Page<DisplayExtractedPostDto>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Long sessionId,
        @RequestParam(required = false) SocialNetwork socialNetwork,
        @RequestParam(required = false) Double minMacedonianConfidence,
        @RequestParam(required = false) Boolean donated,
        @RequestParam(required = false) String search
    ) {
        PostFilterDto filter = new PostFilterDto(sessionId, socialNetwork, minMacedonianConfidence, donated, search);
        return ResponseEntity.ok(extractedPostApplicationService.findAll(filter, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisplayExtractedPostDto> findById(@PathVariable Long id) {
        return extractedPostApplicationService
            .findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<DisplayExtractedPostDto> deleteById(@PathVariable Long id) {
        return extractedPostApplicationService
            .deleteById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
