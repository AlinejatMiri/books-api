package edu.ku.bookapi.controller;

import edu.ku.bookapi.model.Book;
import edu.ku.bookapi.model.BookInput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * REST controller for the library book resource, published under
 * {@code /api/v3/books}.
 *
 * <p>LAB-03 - <em>Updating and Deleting REST Resources</em>:</p>
 * <ul>
 *     <li>{@code GET    /api/v3/books}          - list all books (200 OK)</li>
 *     <li>{@code PUT    /api/v3/books/{bookId}} - update a book (200 OK / 404 Not Found)</li>
 *     <li>{@code DELETE /api/v3/books/{bookId}} - delete a book (204 No Content / 404 Not Found)</li>
 * </ul>
 *
 * <p>The catalogue is kept <b>in memory</b> (this lab uses no database), so the
 * original three books come back every time the application is restarted. The
 * list is a {@link CopyOnWriteArrayList} because Tomcat serves requests on many
 * threads at once and a plain {@code ArrayList} is not safe to read and modify
 * concurrently.</p>
 */
@RestController
@RequestMapping("/api/v3/books")
public class BookController {

    /** In-memory catalogue - the initial data given in the lab hand-out. */
    private final List<Book> books = new CopyOnWriteArrayList<>(List.of(
            new Book(1L, "Java Programming", "John Smith", 5),
            new Book(2L, "Web Development", "Sara Ahmad", 3),
            new Book(3L, "Database Systems", "Ali Khan", 4)
    ));

    /**
     * Returns every book of the catalogue.
     *
     * <p>Sent before and after PUT / DELETE to prove that the list really
     * changed.</p>
     *
     * @return {@code 200 OK} with the whole catalogue as a JSON array
     */
    @GetMapping
    public List<Book> getAllBooks() {
        return books;
    }

    /**
     * Updates an existing book - the HTTP PUT of the lab.
     *
     * <p>The book is looked up with the id taken from the URL, and its title,
     * author and available copies are replaced with the values of the request
     * body. The original book id is kept unchanged. Because {@link Book} is an
     * immutable record, the updated instance replaces the old one at the same
     * position in the list.</p>
     *
     * @param bookId identifier from the URL, e.g. {@code 2} in
     *               {@code PUT /api/v3/books/2}
     * @param input  new values from the JSON request body ({@link BookInput})
     * @return {@code 200 OK} with the updated book, or {@code 404 Not Found}
     *         when no book has that id
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<?> updateBook(
            @PathVariable Long bookId,
            @RequestBody BookInput input
    ) {
        // 1. Search for the book using bookId
        Optional<Book> existingBook = findById(bookId);
        if (existingBook.isEmpty()) {
            // 5. No book with that id -> 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // 2. Create the updated Book object (the id comes from the URL, never from the body)
        Book updatedBook = new Book(bookId, input.title(), input.author(), input.availableCopies());

        // 3. Replace the old book in the list
        books.set(books.indexOf(existingBook.get()), updatedBook);

        // 4. Return the updated book with HTTP 200 OK
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Deletes a book from the catalogue - the HTTP DELETE of the lab.
     *
     * @param bookId identifier from the URL, e.g. {@code 3} in
     *               {@code DELETE /api/v3/books/3}
     * @return {@code 204 No Content} when the book was removed (success has no
     *         body to return), or {@code 404 Not Found} when no book has that id
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        // 1. Search for the book
        Optional<Book> existingBook = findById(bookId);
        if (existingBook.isEmpty()) {
            // 4. No book with that id -> 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // 2. Remove it from the list
        books.remove(existingBook.get());

        // 3. Successfully deleted -> 204 No Content
        return ResponseEntity.noContent().build();
    }

    /**
     * Finds a book by its identifier.
     *
     * @param bookId identifier to look for
     * @return the matching book, or an empty {@link Optional} when it does not exist
     */
    private Optional<Book> findById(Long bookId) {
        return books.stream()
                .filter(book -> book.id().equals(bookId))
                .findFirst();
    }
}
