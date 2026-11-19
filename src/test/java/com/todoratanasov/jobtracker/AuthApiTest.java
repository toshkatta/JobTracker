package com.todoratanasov.jobtracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class AuthApiTest extends AbstractIntegrationTest {

    @Test
    void register_withDuplicteEmail_returns409() throws Exception {
        String email = "user@provider.com";
        String credentials = """
                {"email": "%s", "password": "Asd-qwe-123"}"""
                .formatted(email);

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated());

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        String email = "user@provider.com";

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"email": "%s", "password": "Asd-qwe-123"}""".formatted(email)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"email": "%s", "password": "completely-wrong-password"}""".formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withUnknownEmail_returns401() throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"email": "asd@qwe.com", "password": "completely-wrong-password"}"""))
                .andExpect(status().isUnauthorized());
    }
}
