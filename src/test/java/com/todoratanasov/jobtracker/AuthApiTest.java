package com.todoratanasov.jobtracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

public class AuthApiTest extends AbstractIntegrationTest {

  private static final String PASSWORD = "Asd-qwe-123";

  private static String credentials(String email, String password) {
    return """
        {"email": "%s", "password": "%s"}""".formatted(email, password);
  }

  private ResultActions postRefresh(String refreshToken) throws Exception {
    return mockMvc.perform(
        post(REFRESH_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken": "%s"}""".formatted(refreshToken))
    );
  }

  private ResultActions postLogout(String refreshToken) throws Exception {
    return mockMvc.perform(
        post(LOGOUT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken": "%s"}""".formatted(refreshToken))
    );
  }

  @Test
  void register_withDuplicateEmail_returns409() throws Exception {
    String body = credentials(UUID.randomUUID() + "@example.com", PASSWORD);

    mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());

    mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict());
  }

  @Test
  void register_withDuplicateNormalizedEmail_returns409() throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    String lowercaseEmailBody = credentials(email, PASSWORD);
    String uppercaseEmailBody = credentials(email.toUpperCase(Locale.ROOT), PASSWORD);

    mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(lowercaseEmailBody))
        .andExpect(status().isCreated());

    mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(uppercaseEmailBody))
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

  @Test
  void refresh_returnsNewTokens_andTheNewAccessTokenWorks() throws Exception {
    TestUser user = registerAndLogin();

    TokenBody renewed = readBody(postRefresh(user.refreshToken()).andExpect(status().isOk()), TokenBody.class);

    assertThat(renewed.refreshToken()).isNotBlank().isNotEqualTo(user.refreshToken());
    mockMvc.perform(
        get(JobApplicationClient.URL)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + renewed.accessToken())
    )
        .andExpect(status().isOk());
  }

  @Test
  void refresh_withAnAlreadyUsedToken_returns401() throws Exception {
    TestUser user = registerAndLogin();

    postRefresh(user.refreshToken()).andExpect(status().isOk());
    postRefresh(user.refreshToken()).andExpect(status().isUnauthorized());
  }

  @Test
  void refresh_reuseOfAnOldToken_revokesTheWholeFamily() throws Exception {
    TestUser user = registerAndLogin();
    TokenBody second = readBody(postRefresh(user.refreshToken()).andExpect(status().isOk()), TokenBody.class);

    postRefresh(user.refreshToken()).andExpect(status().isUnauthorized());
    postRefresh(second.refreshToken()).andExpect(status().isUnauthorized());
  }

  @Test
  void refresh_withUnknownToken_returns401() throws Exception {
    postRefresh("unknown-token").andExpect(status().isUnauthorized());
  }

  @Test
  void logout_revokesTheRefreshToken() throws Exception {
    TestUser user = registerAndLogin();

    postLogout(user.refreshToken()).andExpect(status().isNoContent());
    postRefresh(user.refreshToken()).andExpect(status().isUnauthorized());
  }

  @Test
  void logout_withUnknownToken_stillReturns204() throws Exception {
    postLogout("unknown-token").andExpect(status().isNoContent());
  }

  @Test
  void refresh_withBlankToken_returns400() throws Exception {
    postRefresh("").andExpect(status().isBadRequest());
  }
}
