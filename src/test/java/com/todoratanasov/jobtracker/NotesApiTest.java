package com.todoratanasov.jobtracker;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

public class NotesApiTest extends AbstractIntegrationTest {

    record NoteBody(UUID id, UUID jobApplicationId, String content, Boolean important) {
    }

    private static final String NOTE_URL = APPLICATION_NOTES_URL + "/{id}";

    protected NoteBody craeteNote(TestUser user, UUID jobApplicationId, String content, Boolean important)
            throws Exception {
        return readBody(
                mockMvc.perform(
                        post(APPLICATION_NOTES_URL, jobApplicationId)
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
        ApplicationBody billa = craeteApplication(user, "Billa");
        String noteBody = "Test note content";
        boolean isNoteImportant = false;

        var created = craeteNote(user, billa.id(), noteBody, isNoteImportant);

        assertThat(created.id()).isNotNull();
        assertThat(created).isEqualTo(new NoteBody(created.id(), billa.id(), noteBody, isNoteImportant));

        var fetched = readBody(
                mockMvc.perform(
                        get(APPLICATION_NOTES_URL, billa.id())
                                .header(HttpHeaders.AUTHORIZATION, user.authorization())
                )
                        .andExpect(status().isOk()),
                NoteBody[].class
        );

        assertThat(fetched).containsExactly(created);
    }

    @Test
    void delete_deletesOnlyTheRequestedNote() throws Exception {
        TestUser user = registerAndLogin();
        ApplicationBody olympus = craeteApplication(user, "Olympus");
        NoteBody firstNote = craeteNote(user, olympus.id(), "Test first importnant note", true);
        NoteBody secondNote = craeteNote(user, olympus.id(), "Test second note", false);
        NoteBody thirdNote = craeteNote(user, olympus.id(), "Test third note", false);

        var notesBeforeDelete = readBody(
                mockMvc.perform(
                        get(APPLICATION_NOTES_URL, olympus.id())
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
                        get(APPLICATION_NOTES_URL, olympus.id())
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
        ApplicationBody rodnaStryaha = craeteApplication(user, "Rodna Stryaha");

        ErrorBody error = readBody(
                mockMvc.perform(
                        post(APPLICATION_NOTES_URL, rodnaStryaha.id())
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
}
