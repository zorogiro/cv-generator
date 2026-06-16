package tn.esprit.cv_generator.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import tn.esprit.cv_generator.exception.AiException;

@RequiredArgsConstructor
public class CvAiServiceImpl implements CvAiService {

    private final ChatClient chatClient;

    @Override
    public String generateSummary(String rawInput) {
        try {
            return chatClient.prompt()
                    .user("Write a professional CV summary based on these facts:\n" + rawInput)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new AiException("Failed to generate summary", e);
        }
    }

    @Override
    public String improveDescription(String jobDescription) {
        try {
            return chatClient.prompt()
                    .user("Improve this job description for a CV:\n" + jobDescription)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new AiException("Failed to improve description", e);
        }
    }
}
