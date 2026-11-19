package com.todoratanasov.jobtracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.todoratanasov.jobtracker.controller.ApiPaths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

public class NotesApiTest extends AbstractIntegrationTest {

    record NoteBody(UUID id, UUID jobApplicationId, String content, Boolean important) {
    }

    private static final String NOTES_URL = ApiPaths.API_PREFIX + ApiPaths.JOB_APPLICATION_NOTES;
    private static final String NOTE_URL = NOTES_URL + "/{id}";

    private JobApplicationClient jobApplications;

    @BeforeEach
    void setUp() {
        jobApplications = new JobApplicationClient(mockMvc, jsonMapper);
    }

    protected NoteBody craeteNote(TestUser user, UUID jobApplicationId, String content, Boolean important)
            throws Exception {
        return readBody(
                mockMvc.perform(
                        post(NOTES_URL, jobApplicationId)
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"content": "%s", "important":%b}""".formatted(content, important))
                )
                        .andExpect(status().isCreated()),
                NoteBody.class
        );
    }

    @Test
    void create_returnsTheCreatedNote_andItCanBeFetched() throws Exception {
        TestUser user = registerAndLogin();
        JobApplicationBody billa = jobApplications.create(user, "Billa");
        String noteBody = "Test note content";
        boolean isNoteImportant = false;

        var created = craeteNote(user, billa.id(), noteBody, isNoteImportant);

        assertThat(created.id()).isNotNull();
        assertThat(created).isEqualTo(new NoteBody(created.id(), billa.id(), noteBody, isNoteImportant));

        var fetched = readBody(
                mockMvc.perform(
                        get(NOTES_URL, billa.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(fetched).containsExactly(created);
    }

    @Test
    void create_returnsNotesInCreationOrder() throws Exception {
        TestUser user = registerAndLogin();
        JobApplicationBody dayAndNight = jobApplications.create(user, "DayAndNight");
        NoteBody firstNote = craeteNote(user, dayAndNight.id(), "Test first note", false);
        NoteBody secondNote = craeteNote(user, dayAndNight.id(), "Test second note", true);
        NoteBody thirdNote = craeteNote(user, dayAndNight.id(), "Test third note", false);

        var notes = readBody(
                mockMvc.perform(
                        get(NOTES_URL, dayAndNight.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(notes).containsExactly(firstNote, secondNote, thirdNote);
        assertThat(notes.length).isEqualTo(3);
    }

    @Test
    void delete_deletesOnlyTheRequestedNote() throws Exception {
        TestUser user = registerAndLogin();
        JobApplicationBody olympus = jobApplications.create(user, "Olympus");
        NoteBody firstNote = craeteNote(user, olympus.id(), "Test first importnant note", true);
        NoteBody secondNote = craeteNote(user, olympus.id(), "Test second note", false);
        NoteBody thirdNote = craeteNote(user, olympus.id(), "Test third note", false);

        var notesBeforeDelete = readBody(
                mockMvc.perform(
                        get(NOTES_URL, olympus.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(notesBeforeDelete).containsExactlyInAnyOrder(firstNote, secondNote, thirdNote);
        assertThat(notesBeforeDelete.length).isEqualTo(3);

        mockMvc.perform(
                delete(NOTE_URL, olympus.id(), secondNote.id())
                        .header(HttpHeaders.AUTHORIZATION, user.authorization())
        )
                .andExpect(status().isNoContent());

        var notesAfterDelete = readBody(
                mockMvc.perform(
                        get(NOTES_URL, olympus.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(notesAfterDelete).containsExactlyInAnyOrder(firstNote, thirdNote);
        assertThat(notesAfterDelete.length).isEqualTo(2);
    }

    @Test
    void create_withBlankContent_returns400_listingRequiredContentField() throws Exception {
        TestUser user = registerAndLogin();
        JobApplicationBody rodnaStryaha = jobApplications.create(user, "Rodna Stryaha");

        ErrorBody error = readBody(
                mockMvc.perform(
                        post(NOTES_URL, rodnaStryaha.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"content": "", "important": false}""")
                )
                        .andExpect(status().isBadRequest()),
                ErrorBody.class
        );

        assertThat(error.errors())
                .extracting(FieldErrorBody::field)
                .containsExactly("content");
    }

    @Test
    void list_fromAnotherUser_returns404() throws Exception {
        TestUser owner = registerAndLogin();
        TestUser intruder = registerAndLogin();
        JobApplicationBody toyota = jobApplications.create(owner, "Toyota");
        String intruderAuth = intruder.authorization();

        mockMvc.perform(get(NOTES_URL, toyota.id()).header(HttpHeaders.AUTHORIZATION, intruderAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_fromAnotherUser_returns404() throws Exception {
        TestUser owner = registerAndLogin();
        TestUser intruder = registerAndLogin();
        JobApplicationBody honda = jobApplications.create(owner, "Honda");
        String intruderAuth = intruder.authorization();

        mockMvc.perform(
                post(NOTES_URL, honda.id())
                        .header(HttpHeaders.AUTHORIZATION, intruderAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "Test", "important": false}""")
        )
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_fromAnotherUser_returns404() throws Exception {
        TestUser owner = registerAndLogin();
        TestUser intruder = registerAndLogin();
        JobApplicationBody mazda = jobApplications.create(owner, "Mazda");
        NoteBody importantNote = craeteNote(owner, mazda.id(), "Test importnant note", true);
        NoteBody unimportantNote = craeteNote(owner, mazda.id(), "Test unimportnant note", false);
        String intruderAuth = intruder.authorization();

        mockMvc.perform(
                delete(NOTE_URL, mazda.id(), importantNote.id()).header(HttpHeaders.AUTHORIZATION, intruderAuth)
        )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                delete(NOTE_URL, mazda.id(), unimportantNote.id()).header(HttpHeaders.AUTHORIZATION, intruderAuth)
        )
                .andExpect(status().isNotFound());
    }
}
