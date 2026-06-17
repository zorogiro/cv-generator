package tn.esprit.cv_generator.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.cv_generator.exception.AiException;
import tn.esprit.cv_generator.repository.ResumeRepository;
import tn.esprit.cv_generator.repository.UserRepository;
import tn.esprit.cv_generator.service.ai.CvAiService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Ai502Test {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ResumeRepository resumeRepository;
    @Autowired UserRepository userRepository;
    @MockitoBean CvAiService cvAiService;

    private String token;
    private long resumeId;

    @BeforeEach
    void setup() throws Exception {
        resumeRepository.deleteAll();
        userRepository.deleteAll();
        token = acquireToken("aiuser@example.com", "password123");
        resumeId = createResume(token);
        when(cvAiService.generateSummary(any())).thenThrow(new AiException("Claude is down", null));
        when(cvAiService.improveDescription(any())).thenThrow(new AiException("Claude is down", null));
    }

    @Test
    void generateSummary_whenAiThrows_returns502() throws Exception {
        mockMvc.perform(post("/api/resumes/" + resumeId + "/generate-summary")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawInput":"5 years backend, Java, Spring"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("AI Service Error"))
                .andExpect(jsonPath("$.message").value("Claude is down"));
    }

    @Test
    void improveDescription_whenAiThrows_returns502() throws Exception {
        mockMvc.perform(post("/api/resumes/" + resumeId + "/improve-description")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"worked on a project"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("AI Service Error"))
                .andExpect(jsonPath("$.message").value("Claude is down"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
    }

    private String acquireToken(String email, String password) throws Exception {
        register(email, password);
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private long createResume(String authToken) throws Exception {
        String response = mockMvc.perform(post("/api/resumes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Test User","email":"test@example.com",
                                 "workExperiences":[],"educations":[]}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
