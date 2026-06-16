package tn.esprit.cv_generator.service.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tn.esprit.cv_generator.dto.ResumeResponse;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CvPdfServiceImpl implements CvPdfService {

    @Qualifier("pdfTemplateEngine")
    private final TemplateEngine templateEngine;

    @Override
    public String renderHtml(ResumeResponse resume) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("resume", resume);
        return templateEngine.process("resume", context);
    }

    @Override
    public byte[] generatePdf(ResumeResponse resume) {
        String html = renderHtml(resume);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"),
                    "DejaVuSans");
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed for resume: " + resume.getId(), e);
        }
    }
}
