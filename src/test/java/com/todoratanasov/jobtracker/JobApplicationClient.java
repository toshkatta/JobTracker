package com.todoratanasov.jobtracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todoratanasov.jobtracker.controller.ApiPaths;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

final class JobApplicationClient {

  static final String URL = ApiPaths.API_PREFIX + ApiPaths.JOB_APPLICATIONS;

  private final MockMvc mockMvc;
  private final JsonMapper jsonMapper;

  JobApplicationClient(MockMvc mockMvc, JsonMapper jsonMapper) {
    this.mockMvc = mockMvc;
    this.jsonMapper = jsonMapper;
  }

  JobApplicationBody create(TestUser user, String company) throws Exception {
    String json = mockMvc.perform(
        post(URL)
            .header(HttpHeaders.AUTHORIZATION, user.authorization())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {"company":"%s","position":"Dev","status":"APPLIED","salary":5000}"""
                    .formatted(company)
            )
    )
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    return jsonMapper.readValue(json, JobApplicationBody.class);
  }
}