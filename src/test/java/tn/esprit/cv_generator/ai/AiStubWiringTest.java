package tn.esprit.cv_generator.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.cv_generator.service.ai.CvAiService;
import tn.esprit.cv_generator.service.ai.CvAiServiceStub;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AiStubWiringTest {

    @Autowired
    private CvAiService cvAiService;

    @Test
    void withNoApiKey_stubIsActiveCvAiServiceBean() {
        assertThat(cvAiService).isInstanceOf(CvAiServiceStub.class);
    }
}
