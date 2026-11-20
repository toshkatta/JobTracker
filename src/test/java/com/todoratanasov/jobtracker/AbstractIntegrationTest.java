package com.todoratanasov.jobtracker;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.todoratanasov.jobtracker.controller.ApiPaths;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

  protected static final String HEALTH_URL = ApiPaths.API_PREFIX + ApiPaths.HEALTH;
  protected static final String REGISTER_URL = ApiPaths.API_PREFIX + ApiPaths.AUTH + ApiPaths.REGISTER;
  protected static final String LOGIN_URL = ApiPaths.API_PREFIX + ApiPaths.AUTH + ApiPaths.LOGIN;
  protected static final String REFRESH_URL = ApiPaths.API_PREFIX + ApiPaths.AUTH + ApiPaths.REFRESH;
  protected static final String LOGOUT_URL = ApiPaths.API_PREFIX + ApiPaths.AUTH + ApiPaths.LOGOUT;

  // Test-local views of the JSON the API returns. Deliberately NOT the production DTOs,
  // so a renamed field in the real response makes these tests fail.
  record TokenBody(String accessToken, String refreshToken) {
  }

  record FieldErrorBody(String field, String message) {
  }

  record ErrorBody(String title, String detail, List<FieldErrorBody> errors) {
  }

  @ServiceConnection
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  static {
    postgres.start();
  }

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected JsonMapper jsonMapper;

  protected <T> T readBody(ResultActions result, Class<T> type) throws Exception {
    return jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(), type);
  }

  protected TestUser registerAndLogin() throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    String credentials = """
        {"email": "%s", "password": "Asd-qwe-123"}"""
        .formatted(email);

    mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(credentials))
        .andExpect(status().isCreated());

    TokenBody token = readBody(
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(credentials))
            .andExpect(status().isOk()),
        TokenBody.class
    );

    return new TestUser(email, token.accessToken(), token.refreshToken());
  }
}
