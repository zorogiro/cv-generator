package tn.esprit.cv_generator.service.ai;

public interface CvAiService {

    /**
     * Generate a professional summary from raw freeform input provided by the user.
     */
    String generateSummary(String rawInput);

    /**
     * Rewrite a job description bullet to be more impactful and action-verb-led.
     */
    String improveDescription(String jobDescription);
}
