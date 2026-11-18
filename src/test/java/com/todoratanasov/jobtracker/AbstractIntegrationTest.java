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
import org.springframework.http.HttpHeaders;
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
    protected static final String APPLICATIONS_URL = ApiPaths.API_PREFIX + ApiPaths.APPLICATIONS;
    protected static final String APPLICATION_NOTES_URL = ApiPaths.API_PREFIX + ApiPaths.APPLICATION_NOTES;

    // Test-local views of the JSON the API returns. Deliberately NOT the production DTOs,
    // so a renamed field in the real response makes these tests fail.
    record TokenBody(String accessToken) {
    }

    record ApplicationBody(UUID id, String company, String position, String status, Integer salary) {
    }

    record PageInfo(int size, int number, long totalElements, int totalPages) {
    }

    record PageBody(List<ApplicationBody> content, PageInfo page) {
    }

    record FieldErrorBody(String field, String message) {
    }

    record ErrorBody(String title, String detail, List<FieldErrorBody> errors) {
    }

    record TestUser(String email, String token) {
        String authorization() {
            return "Bearer " + token;
        }
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

        return new TestUser(email, token.accessToken());
    }

    protected ApplicationBody craeteApplication(TestUser user, String company) throws Exception {
        return readBody(
                mockMvc.perform(
                        post(APPLICATIONS_URL)
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                                {"company": "%s", "position": "Dev", "status": "APPLIED", "salary": 5000}"""
                                                .formatted(company)
                                )
                )
                        .andExpect(status().isCreated()),
                ApplicationBody.class
        );
    }
}
