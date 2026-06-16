package tn.esprit.cv_generator.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfRendererSmokeTest {

    @Test
    void minimalHtmlProducesNonEmptyPdf() throws Exception {
        String html = "<html><head><style>body{font-family:'DejaVuSans',sans-serif;font-size:12pt;}</style></head>"
                + "<body><h1>Hello</h1><p>Test paragraph.</p></body></html>";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFont(
                () -> PdfRendererSmokeTest.class.getResourceAsStream("/fonts/DejaVuSans.ttf"),
                "DejaVuSans");
        builder.withHtmlContent(html, null);
        builder.toStream(out);
        builder.run();
        byte[] bytes = out.toByteArray();

        try (PDDocument doc = PDDocument.load(bytes)) {
            int pages = doc.getNumberOfPages();
            System.out.println("PDF pages: " + pages + ", bytes: " + bytes.length);
            assertTrue(pages >= 1, "Expected at least 1 page but got " + pages);
            assertTrue(bytes.length > 3000, "Expected > 3KB but got " + bytes.length + " bytes");
        }
    }
}
