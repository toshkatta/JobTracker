package com.todoratanasov.jobtracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class AuthApiTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "Asd-qwe-123";

    private static String credentials(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}""".formatted(email, password);
    }

    @Test
    void register_withDuplicteEmail_returns409() throws Exception {
        String body = credentials(UUID.randomUUID() + "@example.com", PASSWORD);

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withDuplicteNormalizedEmail_returns409() throws Exception {
        String uppercaseEmail = credentials("A@x.com", PASSWORD);
        String lowercaseEmail = credentials("a@x.com", PASSWORD);

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(uppercaseEmail))
                .andExpect(status().isCreated());

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(lowercaseEmail))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withInvalidEmailAndShortPassword_returns400AndListsEmailAndPassword() throws Exception {
        String body = credentials("not-an-email", "abc");

        ErrorBody error = readBody(
                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                        .andExpect(status().isBadRequest()),
                ErrorBody.class
        );

        assertThat(error.errors())
                .extracting(FieldErrorBody::field)
                .containsExactlyInAnyOrder("email", "password");
    }

    @Test
    void login_failsIdentically_forWrongPasswordAndUnknownEmail() throws Exception {
        TestUser user = registerAndLogin();

        String wrongPassword = mockMvc.perform(
                post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(user.email(), "completely-wrong-password"))
        )
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String unknownEmail = mockMvc.perform(
                post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(UUID.randomUUID() + "@example.com", "completely-wrong-password"))
        )
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(wrongPassword).isEqualTo(unknownEmail);
    }
}
