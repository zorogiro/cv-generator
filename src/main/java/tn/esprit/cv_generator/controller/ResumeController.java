package tn.esprit.cv_generator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cv_generator.dto.GenerateSummaryRequest;
import tn.esprit.cv_generator.dto.ImproveDescriptionRequest;
import tn.esprit.cv_generator.dto.ImprovedDescriptionResponse;
import tn.esprit.cv_generator.dto.ResumeRequest;
import tn.esprit.cv_generator.dto.ResumeResponse;
import tn.esprit.cv_generator.dto.SummaryResponse;
import tn.esprit.cv_generator.entity.User;
import tn.esprit.cv_generator.service.ResumeService;
import tn.esprit.cv_generator.service.ai.CvAiService;
import tn.esprit.cv_generator.service.pdf.CvPdfService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final CvPdfService cvPdfService;
    private final CvAiService cvAiService;

    // 1. List — returns ONLY the authenticated user's resumes
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> findAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.findAllByUser(user));
    }

    // 2. Get one — 404 if not found or belongs to a different user
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.findByIdAndUser(id, user));
    }

    // 3. Create — new resume is automatically linked to the authenticated user
    @PostMapping
    public ResponseEntity<ResumeResponse> create(
            @Valid @RequestBody ResumeRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.create(request, user));
    }

    // 4. Update — 404 if not found or belongs to a different user
    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ResumeRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.update(id, request, user));
    }

    // 5. Delete — 404 if not found or belongs to a different user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        resumeService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    // 6. PDF download — ownership verified via findByIdAndUser
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ResumeResponse resume = resumeService.findByIdAndUser(id, user);
        byte[] pdf = cvPdfService.generatePdf(resume);
        String filename = resume.getFullName()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                + "-cv.pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    // 7. HTML preview — ownership verified via findByIdAndUser
    @GetMapping("/{id}/preview")
    public ResponseEntity<String> previewHtml(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ResumeResponse resume = resumeService.findByIdAndUser(id, user);
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(cvPdfService.renderHtml(resume));
    }

    // 8. Generate summary — ownership verified via findByIdAndUser
    @PostMapping("/{id}/generate-summary")
    public ResponseEntity<SummaryResponse> generateSummary(
            @PathVariable Long id,
            @Valid @RequestBody GenerateSummaryRequest request,
            @AuthenticationPrincipal User user) {
        resumeService.findByIdAndUser(id, user); // 404 if not found or wrong owner
        String summary = cvAiService.generateSummary(request.rawInput());
        return ResponseEntity.ok(new SummaryResponse(summary));
    }

    // 9. Improve description — ownership verified via findByIdAndUser
    @PostMapping("/{id}/improve-description")
    public ResponseEntity<ImprovedDescriptionResponse> improveDescription(
            @PathVariable Long id,
            @Valid @RequestBody ImproveDescriptionRequest request,
            @AuthenticationPrincipal User user) {
        resumeService.findByIdAndUser(id, user); // 404 if not found or wrong owner
        String improved = cvAiService.improveDescription(request.description());
        return ResponseEntity.ok(new ImprovedDescriptionResponse(improved));
    }
}
