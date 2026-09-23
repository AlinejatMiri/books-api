package edu.ku.bookapi.model;

/**
 * Inbound payload (request body) used when a client sends new values for a
 * book.
 *
 * <p>The {@code id} is deliberately <b>not</b> part of this model: the id
 * identifies the resource in the URL ({@code /api/v1/books/{bookId}}) and must
 * never be changed by a request body. Keeping the client payload separate from
 * the stored resource is what makes "keep the original book ID unchanged"
 * possible.</p>
 *
 * @param title           new title of the book
 * @param author          new author name
 * @param availableCopies new number of available copies
 */
public record BookInput(
        String title,
        String author,
        int availableCopies
) {
}
