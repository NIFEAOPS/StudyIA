package com.studyia.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import com.studyia.backend.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.ArgumentCaptor;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void stubEmailService() {
        doNothing().when(emailService).sendVerificationCode(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + java.util.UUID.randomUUID() + "@universidad.edu";
    }

    @Test
    void registerCreatesPendingVerification() throws Exception {
        String email = uniqueEmail("student");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"SecurePass1!","name":"Ana"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.message").value("Código de verificación enviado"));
    }

    @Test
    void refreshRotatesCookieAndReturnsNewAccessToken() throws Exception {
        String email = uniqueEmail("refresh");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"%s","password":"SecurePass1!","name":"Luis"}
                    """.formatted(email)))
            .andExpect(status().isOk())
            .andReturn();

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(emailService).sendVerificationCode(eq(email), codeCaptor.capture());
        MvcResult verification = mockMvc.perform(post("/api/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","code":"%s"}
                        """.formatted(email, codeCaptor.getValue())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(cookie().httpOnly("studyia_refresh_token", true))
            .andReturn();

        MockHttpServletResponse registrationResponse = verification.getResponse();
        String refreshCookie = registrationResponse.getCookie("studyia_refresh_token").getValue();

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("studyia_refresh_token", refreshCookie)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(cookie().httpOnly("studyia_refresh_token", true));
        }

        @Test
        void invalidVerificationCodeIsRejected() throws Exception {
        String email = uniqueEmail("invalid-code");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"SecurePass1!","name":"Luis"}
                    """.formatted(email)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","code":"000000"}
                    """.formatted(email)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void protectedRoadmapEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(post("/api/v1/roadmaps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}
