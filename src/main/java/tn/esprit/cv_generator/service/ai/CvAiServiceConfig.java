package tn.esprit.cv_generator.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import tn.esprit.cv_generator.config.AnthropicKeyPresentCondition;

@Configuration
public class CvAiServiceConfig {

    // Evaluated first: real impl only when a non-blank key is present.
    @Bean
    @Conditional(AnthropicKeyPresentCondition.class)
    public CvAiService cvAiService(ChatClient cvWriterChatClient) {
        return new CvAiServiceImpl(cvWriterChatClient);
    }

    // Evaluated second: stub fills in when no CvAiService bean was registered above.
    // @ConditionalOnMissingBean on a @Bean method in @Configuration is deterministic
    // because Spring processes @Bean methods in declaration order within a class.
    @Bean
    @ConditionalOnMissingBean(CvAiService.class)
    public CvAiService cvAiServiceStub() {
        return new CvAiServiceStub();
    }
}
