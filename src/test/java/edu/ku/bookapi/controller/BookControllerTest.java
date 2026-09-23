package edu.ku.bookapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ku.bookapi.model.BookInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link BookController}, mirroring the Postman tests of the
 * LAB-03 hand-out.
 *
 * <p>{@code @WebMvcTest} starts only the web slice (no server socket), so the
 * assertions run against MockMvc. The in-memory catalogue is shared by the
 * controller instance, therefore the context is rebuilt after every test method
 * ({@code @DirtiesContext}) so that each test starts again with the original
 * three books.</p>
 */
@WebMvcTest(BookController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v3/books -> 200 OK with the three initial books")
    void getAllBooks_returnsInitialCatalogue() throws Exception {
        mockMvc.perform(get("/api/v3/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Programming"))
                .andExpect(jsonPath("$[1].title").value("Web Development"))
                .andExpect(jsonPath("$[2].title").value("Database Systems"))
                .andExpect(jsonPath("$[2].availableCopies").value(4));
    }

    @Test
    @DisplayName("PUT /api/v3/books/2 -> 200 OK, keeps the id and replaces the values")
    void updateBook_existingBook_returnsUpdatedBook() throws Exception {
        BookInput input = new BookInput("Modern Web Development", "Sara Ahmad", 6);

        mockMvc.perform(put("/api/v3/books/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Modern Web Development"))
                .andExpect(jsonPath("$.author").value("Sara Ahmad"))
                .andExpect(jsonPath("$.availableCopies").value(6));

        // GET afterwards proves that the list really changed
        mockMvc.perform(get("/api/v3/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Modern Web Development"))
                .andExpect(jsonPath("$[1].availableCopies").value(6));
    }

    @Test
    @DisplayName("PUT /api/v3/books/99 -> 404 Not Found (missing book)")
    void updateBook_missingBook_returns404() throws Exception {
        BookInput input = new BookInput("Ghost Book", "Nobody", 1);

        mockMvc.perform(put("/api/v3/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());

        // The catalogue is untouched by a failed update
        mockMvc.perform(get("/api/v3/books"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("DELETE /api/v3/books/3 -> 204 No Content and the book is removed")
    void deleteBook_existingBook_returns204AndRemovesBook() throws Exception {
        mockMvc.perform(delete("/api/v3/books/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/v3/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("DELETE /api/v3/books/99 -> 404 Not Found (nothing to delete)")
    void deleteBook_missingBook_returns404() throws Exception {
        mockMvc.perform(delete("/api/v3/books/99"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v3/books"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Deleting the same book twice -> 204 and then 404")
    void deleteBook_twice_secondCallReturns404() throws Exception {
        mockMvc.perform(delete("/api/v3/books/3")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v3/books/3")).andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------
    // POST - our own extension, not part of the LAB-03 hand-out
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v3/books -> 201 Created with a Location header and a server-assigned id")
    void createBook_returns201WithLocationAndAssignedId() throws Exception {
        BookInput input = new BookInput("Spring in Action", "Craig Walls", 2);

        mockMvc.perform(post("/api/v3/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v3/books/4")))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("Spring in Action"))
                .andExpect(jsonPath("$.author").value("Craig Walls"))
                .andExpect(jsonPath("$.availableCopies").value(2));

        // the created book is really part of the catalogue now
        mockMvc.perform(get("/api/v3/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[3].id").value(4))
                .andExpect(jsonPath("$[3].title").value("Spring in Action"));
    }

    @Test
    @DisplayName("POST twice -> ids 4 and 5 (the counter only moves forward)")
    void createBook_assignsIncrementingIds() throws Exception {
        BookInput first = new BookInput("First Book", "Author A", 1);
        BookInput second = new BookInput("Second Book", "Author B", 2);

        mockMvc.perform(post("/api/v3/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4));

        mockMvc.perform(post("/api/v3/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));

        mockMvc.perform(get("/api/v3/books"))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[3].title").value("First Book"))
                .andExpect(jsonPath("$[4].title").value("Second Book"));
    }

    @Test
    @DisplayName("A created book can be updated (PUT) and deleted (DELETE) like any other book")
    void createBook_createdBookIsFullyAddressable() throws Exception {
        BookInput input = new BookInput("Temporary Book", "Nobody", 1);
        BookInput update = new BookInput("Temporary Book, 2nd Edition", "Nobody", 3);

        mockMvc.perform(post("/api/v3/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());

        // PUT works on the id the server assigned
        mockMvc.perform(put("/api/v3/books/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("Temporary Book, 2nd Edition"))
                .andExpect(jsonPath("$.availableCopies").value(3));

        // and so does DELETE
        mockMvc.perform(delete("/api/v3/books/4"))
                .andExpect(status().isNoContent());

        // the catalogue is back to the three original books
        mockMvc.perform(get("/api/v3/books"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].id").value(3));
    }
}
