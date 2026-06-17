package tn.esprit.cv_generator.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.cv_generator.repository.ResumeRepository;
import tn.esprit.cv_generator.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeValidationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ResumeRepository resumeRepository;
    @Autowired UserRepository userRepository;

    private String token;

    @BeforeEach
    void setup() throws Exception {
        resumeRepository.deleteAll();
        userRepository.deleteAll();
        token = acquireToken("val@example.com", "password123");
    }

    // ── Field-level validation ────────────────────────────────────────────────

    @Test
    void blankFullName_returns400_andPinsFieldKey() throws Exception {
        mockMvc.perform(post("/api/resumes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"","email":"alice@example.com",
                                 "workExperiences":[],"educations":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fields.fullName").exists());
    }

    @Test
    void invalidEmail_returns400_andPinsFieldKey() throws Exception {
        mockMvc.perform(post("/api/resumes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Alice","email":"not-an-email",
                                 "workExperiences":[],"educations":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    // ── Class-level constraint (@ValidDateRange) ──────────────────────────────
    // @ValidDateRange is on WorkExperienceDto (nested, accessed via @Valid cascade).
    // Whether it surfaces as a FieldError (key = "workExperiences[0]") or an
    // ObjectError (key = "resumeRequest.dateRange") depends on how Spring MVC
    // adapts nested class-level ConstraintViolations. We assert 400 + fields
    // non-empty; the exact key is confirmed by the test-run output below.

    @Test
    void dateRangeViolation_returns400_withNonEmptyFields() throws Exception {
        mockMvc.perform(post("/api/resumes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Alice",
                                  "email": "alice@example.com",
                                  "workExperiences": [{
                                    "company": "Acme",
                                    "title": "Dev",
                                    "startDate": "2020-01-01",
                                    "endDate": "2019-01-01"
                                  }],
                                  "educations": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields['workExperiences[0]']").exists());
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
}
