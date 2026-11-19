package com.todoratanasov.jobtracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.todoratanasov.jobtracker.controller.ApiPaths;

public class JobApplicationApiTest extends AbstractIntegrationTest {

  private static final String JOB_APPLICATIONS_URL = ApiPaths.API_PREFIX + ApiPaths.JOB_APPLICATIONS;
  private static final String JOB_APPLICATION_URL = JOB_APPLICATIONS_URL + "/{id}";
  private static final UUID MISSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  private JobApplicationClient jobApplications;

  record PageBody(List<JobApplicationBody> content, PageInfo page) {
  }

  record PageInfo(int size, int number, long totalElements, int totalPages) {
  }

  @BeforeEach
  void setUp() {
    jobApplications = new JobApplicationClient(mockMvc, jsonMapper);
  }

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

  @Test
  void create_returnsTheCreatedApplication_andItCanBeFetched() throws Exception {
    TestUser user = registerAndLogin();

    var created = readBody(
        mockMvc.perform(
            post(JOB_APPLICATIONS_URL)
                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"company":"Acme","position":"Backend Dev","status":"APPLIED","salary":5000}""")
        )
            .andExpect(status().isCreated())
            .andExpect(header().exists(HttpHeaders.LOCATION)),
        JobApplicationBody.class
    );

    assertThat(created.id()).isNotNull();
    assertThat(created).isEqualTo(new JobApplicationBody(created.id(), "Acme", "Backend Dev", "APPLIED", 5000));

    var fetched = readBody(
        mockMvc.perform(
            get(JOB_APPLICATION_URL, created.id())
                .header(HttpHeaders.AUTHORIZATION, user.authorization())
        )
            .andExpect(status().isOk()),
        JobApplicationBody.class
    );

    assertThat(fetched).isEqualTo(created);
  }

  @Test
  void anotherUsersApplication_looksLikeItDoesNotExist() throws Exception {
    TestUser owner = registerAndLogin();
    TestUser intruder = registerAndLogin();
    JobApplicationBody acme = jobApplications.create(owner, "Acme");
    String intruderAuth = intruder.authorization();

    mockMvc.perform(get(JOB_APPLICATION_URL, acme.id()).header(HttpHeaders.AUTHORIZATION, intruderAuth))
        .andExpect(status().isNotFound());

    mockMvc.perform(delete(JOB_APPLICATION_URL, acme.id()).header(HttpHeaders.AUTHORIZATION, intruderAuth))
        .andExpect(status().isNotFound());

    mockMvc.perform(
        put(JOB_APPLICATION_URL, acme.id())
            .header(HttpHeaders.AUTHORIZATION, intruderAuth)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"company": "Hacked", "position": "Hacker", "status": "OFFER", "salary": 6000}""")
    )
        .andExpect(status().isNotFound());

    // and the owner's application is untouched
    var ownerApplication = readBody(
        mockMvc.perform(
            get(JOB_APPLICATION_URL, acme.id()).header(
                HttpHeaders.AUTHORIZATION, owner.authorization()
            )
        )
            .andExpect(status().isOk()),
        JobApplicationBody.class
    );
    assertThat(ownerApplication).isEqualTo(acme);
  }

  @Test
  void list_returnsOnlyTheCallersApplications() throws Exception {
    TestUser schwarzGruppe = registerAndLogin();
    TestUser metroAG = registerAndLogin();
    JobApplicationBody kaufland = jobApplications.create(schwarzGruppe, "Kaufland");
    JobApplicationBody lidl = jobApplications.create(schwarzGruppe, "Lidl");
    JobApplicationBody metro = jobApplications.create(metroAG, "Metro");

    PageBody schwarzGruppePage = readBody(
        mockMvc.perform(
            get(JOB_APPLICATIONS_URL).header(
                HttpHeaders.AUTHORIZATION, schwarzGruppe.authorization()
            )
        )
            .andExpect(status().isOk()),
        PageBody.class
    );
    PageBody metroAGPage = readBody(
        mockMvc.perform(
            get(JOB_APPLICATIONS_URL).header(
                HttpHeaders.AUTHORIZATION, metroAG.authorization()
            )
        )
            .andExpect(status().isOk()),
        PageBody.class
    );

    assertThat(schwarzGruppePage.content()).containsExactlyInAnyOrder(kaufland, lidl);
    assertThat(schwarzGruppePage.page().totalElements()).isEqualTo(2);
    assertThat(metroAGPage.content()).containsExactly(metro);
    assertThat(metroAGPage.page().totalElements()).isEqualTo(1);
  }

  @Test
  void create_withEmptyBody_returns400_listingEveryRequiredField() throws Exception {
    TestUser user = registerAndLogin();

    ErrorBody error = readBody(
        mockMvc.perform(
            post(JOB_APPLICATIONS_URL)
                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest()),
        ErrorBody.class
    );

    assertThat(error.errors())
        .extracting(FieldErrorBody::field)
        .containsExactlyInAnyOrder("company", "position", "status");
  }

  @Test
  void delete_missingApplication_returns404() throws Exception {
    TestUser user = registerAndLogin();

    mockMvc.perform(
        delete(JOB_APPLICATION_URL, MISSING_ID)
            .header(HttpHeaders.AUTHORIZATION, user.authorization())
    )
        .andExpect(status().isNotFound());
  }

  @Test
  void list_withUnknownSortField_returns400() throws Exception {
    TestUser user = registerAndLogin();

    mockMvc.perform(
        get(JOB_APPLICATIONS_URL)
            .param("sort", "nonsense")
            .header(HttpHeaders.AUTHORIZATION, user.authorization())
    )
        .andExpect(status().isBadRequest());
  }
}
