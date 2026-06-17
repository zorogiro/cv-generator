package tn.esprit.cv_generator.resume;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class AuthBoundaryTest {

    @Autowired MockMvc mockMvc;
    @Autowired ResumeRepository resumeRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void clean() {
        resumeRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── Public endpoints — reachable without a token ──────────────────────────

    @Test
    void register_isPublicAndReturns201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"pub@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void login_isPublicAndReturns200() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk());
    }

    // ── Protected endpoints — 401 without a token ────────────────────────────
    // Spring Security intercepts before the controller touches the DB, so a
    // non-existent id (999) is fine — the resource need not exist to get a 401.

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("protectedEndpoints")
    void protectedEndpoint_withoutToken_returns401(
            String method, String path, String body) throws Exception {
        MockHttpServletRequestBuilder req = switch (method) {
            case "GET"    -> get(path);
            case "POST"   -> post(path).contentType(MediaType.APPLICATION_JSON).content(body);
            case "PUT"    -> put(path).contentType(MediaType.APPLICATION_JSON).content(body);
            case "DELETE" -> delete(path);
            default       -> throw new IllegalArgumentException(method);
        };
        mockMvc.perform(req).andExpect(status().isUnauthorized());
    }

    static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                Arguments.of("GET",    "/api/resumes",                             ""),
                Arguments.of("POST",   "/api/resumes",                             "{\"fullName\":\"x\",\"email\":\"x@x.com\"}"),
                Arguments.of("GET",    "/api/resumes/999",                         ""),
                Arguments.of("PUT",    "/api/resumes/999",                         "{}"),
                Arguments.of("DELETE", "/api/resumes/999",                         ""),
                Arguments.of("GET",    "/api/resumes/999/pdf",                     ""),
                Arguments.of("GET",    "/api/resumes/999/preview",                 ""),
                Arguments.of("POST",   "/api/resumes/999/generate-summary",        "{\"rawInput\":\"x\"}"),
                Arguments.of("POST",   "/api/resumes/999/improve-description",     "{\"description\":\"x\"}")
        );
    }
}
