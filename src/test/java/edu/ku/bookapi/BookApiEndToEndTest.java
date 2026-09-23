package edu.ku.bookapi;

import edu.ku.bookapi.model.Book;
import edu.ku.bookapi.model.BookInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test over real HTTP, executed in the same order as the Postman
 * table of the LAB-03 hand-out.
 *
 * <p>{@code webEnvironment = RANDOM_PORT} starts the real embedded Tomcat on a
 * free port and talks to it with {@link TestRestTemplate}, so status codes,
 * serialization and the HTTP verbs behave exactly as in Postman. The test
 * methods share one application context and therefore one in-memory catalogue,
 * which is why they are ordered.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookApiEndToEndTest {

    private static final String BOOKS_URL = "/api/v1/books";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    @DisplayName("1. GET /api/v1/books -> 200 OK with 3 books")
    void getAllBooks() {
        ResponseEntity<Book[]> response = restTemplate.getForEntity(BOOKS_URL, Book[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()[0].title()).isEqualTo("Java Programming");
        assertThat(response.getBody()[1].availableCopies()).isEqualTo(3);
    }

    @Test
    @Order(2)
    @DisplayName("2. PUT /api/v1/books/2 -> 200 OK with the updated book")
    void updateBook() {
        BookInput input = new BookInput("Modern Web Development", "Sara Ahmad", 6);

        ResponseEntity<Book> response =
                restTemplate.exchange(BOOKS_URL + "/2", HttpMethod.PUT, new HttpEntity<>(input), Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(2L);
        assertThat(response.getBody().title()).isEqualTo("Modern Web Development");
        assertThat(response.getBody().availableCopies()).isEqualTo(6);
    }

    @Test
    @Order(3)
    @DisplayName("3. PUT /api/v1/books/99 -> 404 Not Found")
    void updateMissingBook() {
        BookInput input = new BookInput("Ghost Book", "Nobody", 1);

        ResponseEntity<Book> response =
                restTemplate.exchange(BOOKS_URL + "/99", HttpMethod.PUT, new HttpEntity<>(input), Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(4)
    @DisplayName("4. DELETE /api/v1/books/3 -> 204 No Content")
    void deleteBook() {
        ResponseEntity<Void> response = restTemplate.exchange(BOOKS_URL + "/3", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @Order(5)
    @DisplayName("5. DELETE /api/v1/books/99 -> 404 Not Found")
    void deleteMissingBook() {
        ResponseEntity<Void> response = restTemplate.exchange(BOOKS_URL + "/99", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(6)
    @DisplayName("6. GET after PUT and DELETE -> the list really changed")
    void getBooksAfterMutations() {
        ResponseEntity<Book[]> response = restTemplate.getForEntity(BOOKS_URL, Book[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .extracting(Book::id)
                .containsExactly(1L, 2L);           // book 3 was deleted
        assertThat(response.getBody()[1].title()).isEqualTo("Modern Web Development"); // book 2 was updated
        assertThat(response.getBody()[1].availableCopies()).isEqualTo(6);
    }
}
