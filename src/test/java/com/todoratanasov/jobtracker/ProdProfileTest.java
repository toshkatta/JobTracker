package com.todoratanasov.jobtracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.todoratanasov.jobtracker.controller.ApiPaths;

@ActiveProfiles("prod")
class ProdProfileTest extends AbstractIntegrationTest {

  @Test
  void docs_areDisabledByDefault_inProd() throws Exception {
    mockMvc.perform(get(ApiPaths.API_DOCS)).andExpect(status().isNotFound());
    mockMvc.perform(get(ApiPaths.SWAGGER_UI + ".html")).andExpect(status().isNotFound());
  }
}
