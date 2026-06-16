package tn.esprit.cv_generator.auth;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ResumeRepository resumeRepository;

    @BeforeEach
    void clean() {
        // FK order: resumes reference users, so delete resumes first
        resumeRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── 1. Register ───────────────────────────────────────────────────────────

    @Test
    void register_returns201_withToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"password123","displayName":"Alice"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ── 2. Login ──────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        registerUser("bob@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bob@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ── 3. No token → 401 ────────────────────────────────────────────────────

    @Test
    void resumeEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/resumes"))
                .andExpect(status().isUnauthorized());
    }

    // ── 4. Cross-user access → 404 ───────────────────────────────────────────

    @Test
    void accessOtherUsersResume_returns404() throws Exception {
        // User A registers, creates a resume
        String tokenA = getToken("userA@example.com", "passwordA1");
        String createBody = """
                {"fullName":"Alice","email":"alice@example.com",
                 "workExperiences":[],"educations":[]}
                """;
        String createResponse = mockMvc.perform(post("/api/resumes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long resumeId = objectMapper.readTree(createResponse).get("id").asLong();

        // User B registers and tries to read user A's resume
        String tokenB = getToken("userB@example.com", "passwordB1");
        mockMvc.perform(get("/api/resumes/" + resumeId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void registerUser(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
    }

    private String getToken(String email, String password) throws Exception {
        registerUser(email, password);
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
