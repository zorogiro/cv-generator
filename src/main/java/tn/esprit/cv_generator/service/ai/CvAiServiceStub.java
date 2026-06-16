package tn.esprit.cv_generator.service.ai;

public class CvAiServiceStub implements CvAiService {

    @Override
    public String generateSummary(String rawInput) {
        return "[AI unavailable — set ANTHROPIC_API_KEY to activate] " + rawInput;
    }

    @Override
    public String improveDescription(String jobDescription) {
        return "[AI unavailable — set ANTHROPIC_API_KEY to activate] " + jobDescription;
    }
}
