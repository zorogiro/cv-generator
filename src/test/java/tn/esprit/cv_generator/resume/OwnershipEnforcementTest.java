package tn.esprit.cv_generator.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tn.esprit.cv_generator.repository.ResumeRepository;
import tn.esprit.cv_generator.repository.UserRepository;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnershipEnforcementTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ResumeRepository resumeRepository;
    @Autowired UserRepository userRepository;

    private String tokenB;
    private long resumeId;

    @BeforeAll
    void setup() throws Exception {
        resumeRepository.deleteAll();
        userRepository.deleteAll();

        // User A creates a resume
        String tokenA = acquireToken("ownerA@example.com", "passwordA1!");
        String response = mockMvc.perform(post("/api/resumes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Alice","email":"alice@example.com",
                                 "workExperiences":[],"educations":[]}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        resumeId = objectMapper.readTree(response).get("id").asLong();

        // User B has no resumes — will try to access user A's
        tokenB = acquireToken("intruderB@example.com", "passwordB1!");
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("crossUserEndpoints")
    void crossUser_allEndpoints_return404(String method, String pathTemplate, String body)
            throws Exception {
        String path = pathTemplate.replace("{id}", String.valueOf(resumeId));
        MockHttpServletRequestBuilder req = switch (method) {
            case "GET"    -> get(path);
            case "PUT"    -> put(path).contentType(MediaType.APPLICATION_JSON).content(body);
            case "DELETE" -> delete(path);
            case "POST"   -> post(path).contentType(MediaType.APPLICATION_JSON).content(body);
            default       -> throw new IllegalArgumentException("Unknown method: " + method);
        };
        mockMvc.perform(req.header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    static Stream<Arguments> crossUserEndpoints() {
        String validResume = """
                {"fullName":"Bob","email":"bob@example.com",
                 "workExperiences":[],"educations":[]}
                """;
        return Stream.of(
                Arguments.of("GET",    "/api/resumes/{id}",                      ""),
                Arguments.of("PUT",    "/api/resumes/{id}",                      validResume),
                Arguments.of("DELETE", "/api/resumes/{id}",                      ""),
                Arguments.of("GET",    "/api/resumes/{id}/pdf",                  ""),
                Arguments.of("GET",    "/api/resumes/{id}/preview",              ""),
                Arguments.of("POST",   "/api/resumes/{id}/generate-summary",     "{\"rawInput\":\"input\"}"),
                Arguments.of("POST",   "/api/resumes/{id}/improve-description",  "{\"description\":\"desc\"}")
        );
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
