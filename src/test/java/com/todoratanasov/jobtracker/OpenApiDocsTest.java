package com.todoratanasov.jobtracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;

import com.todoratanasov.jobtracker.controller.ApiPaths;

public class OpenApiDocsTest extends AbstractIntegrationTest {

  @Test
  void openApiDocs_arePublic_andDescribeTheRealRoutes() throws Exception {
    mockMvc.perform(get(ApiPaths.API_DOCS))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/job-applications']").exists())
        .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
        .andExpect(jsonPath("$.paths['/api/job-applications/{id}'].get.responses['200']").exists())
        .andExpect(jsonPath("$.paths['/api/job-applications/{id}'].get.responses['404']").exists())
        .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['401']").exists())
        .andExpect(jsonPath("$.components.schemas.Problem").exists());
  }

  @Test
  void openApiDocs_listTagsInTheIntendedOrder() throws Exception {
    mockMvc.perform(get(ApiPaths.API_DOCS))
        .andExpect(jsonPath("$.tags[0].name").value("Health"))
        .andExpect(jsonPath("$.tags[1].name").value("Authentication"))
        .andExpect(jsonPath("$.tags[2].name").value("Job applications"))
        .andExpect(jsonPath("$.tags[3].name").value("Notes"));
  }
}
