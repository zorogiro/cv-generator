package tn.esprit.cv_generator.service.pdf;

import tn.esprit.cv_generator.dto.ResumeResponse;

public interface CvPdfService {

    /**
     * Render the resume to a PDF byte array.
     * The caller is responsible for loading and supplying the ResumeResponse,
     * which keeps this service free of repository dependencies and avoids a
     * second DB round-trip when the controller already fetched the entity.
     */
    byte[] generatePdf(ResumeResponse resume);

    /**
     * Render the resume to an XHTML string.
     * Used by generatePdf internally and exposed for the /preview endpoint
     * so layout can be iterated in a browser without regenerating PDFs.
     */
    String renderHtml(ResumeResponse resume);
}
