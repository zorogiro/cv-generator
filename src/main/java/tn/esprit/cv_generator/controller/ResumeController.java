package tn.esprit.cv_generator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.cv_generator.dto.GenerateSummaryRequest;
import tn.esprit.cv_generator.dto.ImproveDescriptionRequest;
import tn.esprit.cv_generator.dto.ImprovedDescriptionResponse;
import tn.esprit.cv_generator.dto.ResumeRequest;
import tn.esprit.cv_generator.dto.ResumeResponse;
import tn.esprit.cv_generator.dto.SummaryResponse;
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

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> findAll() {
        return ResponseEntity.ok(resumeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(resumeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> create(@Valid @RequestBody ResumeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> update(@PathVariable Long id, @Valid @RequestBody ResumeRequest request) {
        return ResponseEntity.ok(resumeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        ResumeResponse resume = resumeService.findById(id);
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

    @GetMapping("/{id}/preview")
    public ResponseEntity<String> previewHtml(@PathVariable Long id) {
        ResumeResponse resume = resumeService.findById(id);
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(cvPdfService.renderHtml(resume));
    }

    @PostMapping("/{id}/generate-summary")
    public ResponseEntity<SummaryResponse> generateSummary(
            @PathVariable Long id,
            @Valid @RequestBody GenerateSummaryRequest request) {
        resumeService.findById(id); // ensures resume exists → 404 if not
        String summary = cvAiService.generateSummary(request.rawInput());
        return ResponseEntity.ok(new SummaryResponse(summary));
    }

    @PostMapping("/{id}/improve-description")
    public ResponseEntity<ImprovedDescriptionResponse> improveDescription(
            @PathVariable Long id,
            @Valid @RequestBody ImproveDescriptionRequest request) {
        resumeService.findById(id); // ensures resume exists → 404 if not
        String improved = cvAiService.improveDescription(request.description());
        return ResponseEntity.ok(new ImprovedDescriptionResponse(improved));
    }
}
