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

    record NoteBody(UUID id, UUID jobApplicationId, String content, boolean important) {
    }

    private static final String NOTES_URL = ApiPaths.API_PREFIX + ApiPaths.JOB_APPLICATION_NOTES;
    private static final String NOTE_URL = NOTES_URL + "/{id}";

    private JobApplicationClient jobApplications;

    @BeforeEach
    void setUp() {
        jobApplications = new JobApplicationClient(mockMvc, jsonMapper);
    }

    private NoteBody createNote(TestUser user, UUID jobApplicationId, String content, Boolean important)
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

        var created = createNote(user, billa.id(), noteBody, isNoteImportant);

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
        NoteBody firstNote = createNote(user, dayAndNight.id(), "Test first note", false);
        NoteBody secondNote = createNote(user, dayAndNight.id(), "Test second note", true);
        NoteBody thirdNote = createNote(user, dayAndNight.id(), "Test third note", false);

        var notes = readBody(
                mockMvc.perform(
                        get(NOTES_URL, dayAndNight.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(notes).containsExactly(firstNote, secondNote, thirdNote);
        assertThat(notes).hasSize(3);
    }

    @Test
    void delete_deletesOnlyTheRequestedNote() throws Exception {
        TestUser user = registerAndLogin();
        JobApplicationBody olympus = jobApplications.create(user, "Olympus");
        NoteBody firstNote = createNote(user, olympus.id(), "Test first importnant note", true);
        NoteBody secondNote = createNote(user, olympus.id(), "Test second note", false);
        NoteBody thirdNote = createNote(user, olympus.id(), "Test third note", false);

        var notesBeforeDelete = readBody(
                mockMvc.perform(
                        get(NOTES_URL, olympus.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(notesBeforeDelete).containsExactlyInAnyOrder(firstNote, secondNote, thirdNote);
        assertThat(notesBeforeDelete).hasSize(3);

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
        assertThat(notesAfterDelete).hasSize(2);
    }

    @Test
    void create_withEmptyBody_returns400_listingOnlyContent() throws Exception {
        TestUser user = registerAndLogin();
        JobApplicationBody solevita = jobApplications.create(user, "Solevita");

        ErrorBody error = readBody(
                mockMvc.perform(
                        post(NOTES_URL, solevita.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                        .andExpect(status().isBadRequest()),
                ErrorBody.class
        );

        assertThat(error.errors())
                .extracting(FieldErrorBody::field)
                .containsExactly("content");
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
    void create_withoutImportant_defaultsToFalse() throws Exception {
        TestUser user = registerAndLogin();
        JobApplicationBody bozhetsi = jobApplications.create(user, "Bozhetsi");

        var note = readBody(
                mockMvc.perform(
                        post(NOTES_URL, bozhetsi.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"content": "Test note"}""")
                )
                        .andExpect(status().isCreated()),
                NoteBody.class
        );

        assertThat(note.important()).isEqualTo(false);
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
        NoteBody importantNote = createNote(owner, mazda.id(), "Test importnant note", true);
        NoteBody unimportantNote = createNote(owner, mazda.id(), "Test unimportnant note", false);
        String intruderAuth = intruder.authorization();

        mockMvc.perform(
                delete(NOTE_URL, mazda.id(), importantNote.id()).header(HttpHeaders.AUTHORIZATION, intruderAuth)
        )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                delete(NOTE_URL, mazda.id(), unimportantNote.id()).header(HttpHeaders.AUTHORIZATION, intruderAuth)
        )
                .andExpect(status().isNotFound());

        var originalNotes = readBody(
                mockMvc.perform(
                        get(NOTES_URL, mazda.id())
                                .header(HttpHeaders.AUTHORIZATION, owner.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(originalNotes).containsExactly(importantNote, unimportantNote);
    }

    @Test
    void delete_fromAnotherJobApplication_returns404() throws Exception {
        TestUser owner = registerAndLogin();
        JobApplicationBody suzuki = jobApplications.create(owner, "Suzuki");
        JobApplicationBody yamaha = jobApplications.create(owner, "Yamaha");
        NoteBody suzukiNote = createNote(owner, suzuki.id(), "hayabusa goes fast", true);
        NoteBody yamahaNote = createNote(owner, yamaha.id(), "r1 sells", false);

        mockMvc.perform(
                delete(NOTE_URL, suzuki.id(), yamahaNote.id()).header(HttpHeaders.AUTHORIZATION, owner.authorization())
        )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                delete(NOTE_URL, yamaha.id(), suzukiNote.id()).header(HttpHeaders.AUTHORIZATION, owner.authorization())
        )
                .andExpect(status().isNotFound());

        var originalSuzukiNotes = readBody(
                mockMvc.perform(
                        get(NOTES_URL, suzuki.id())
                                .header(HttpHeaders.AUTHORIZATION, owner.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(originalSuzukiNotes).containsExactly(suzukiNote);

        var originalYamahaNotes = readBody(
                mockMvc.perform(
                        get(NOTES_URL, yamaha.id())
                                .header(HttpHeaders.AUTHORIZATION, owner.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );
        assertThat(originalYamahaNotes).containsExactly(yamahaNote);
    }
}
