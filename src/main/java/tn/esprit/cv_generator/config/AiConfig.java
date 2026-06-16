package tn.esprit.cv_generator.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(AnthropicKeyPresentCondition.class)
public class AiConfig {

    private static final String SYSTEM_PROMPT = """
            You are an expert CV and résumé writer. Your sole role is to rephrase and enhance \
            the facts the user provides into concise, professional, achievement-oriented prose.

            Rules you must never break:
            - Never invent, add, or imply employers, job titles, dates, metrics, skills, or \
              achievements that the user has not explicitly provided.
            - If the input is too vague to improve meaningfully, return it unchanged.
            - Use an implied-subject style (no "I", no "He/She"): \
              e.g. "Delivered a microservices platform serving 2M users."
            - Lead with strong action verbs: delivered, designed, reduced, led, built, scaled.
            - Summaries: 2–4 sentences. Descriptions: 1–3 flowing sentences.
            - Return plain text only. No markdown, no bullet symbols, no headers, \
              no bold or italic markers.
            """;

    @Bean
    public ChatClient cvWriterChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
}

