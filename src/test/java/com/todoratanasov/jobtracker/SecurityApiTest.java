package com.todoratanasov.jobtracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import com.todoratanasov.jobtracker.controller.ApiPaths;

public class SecurityApiTest extends AbstractIntegrationTest {

  private static final String JOB_APPLICATIONS_URL = ApiPaths.API_PREFIX + ApiPaths.JOB_APPLICATIONS;

  @Test
  void protectedEndopint_withoutToken_returns401() throws Exception {
    mockMvc.perform(get(JOB_APPLICATIONS_URL))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpoint_withTamperedToken_returns401() throws Exception {
    TestUser user = registerAndLogin();

    mockMvc.perform(get(JOB_APPLICATIONS_URL).header(HttpHeaders.AUTHORIZATION, user.authorization() + "x"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void health_isPublic() throws Exception {
    mockMvc.perform(get(HEALTH_URL))
        .andExpect(status().isOk());
  }
}
