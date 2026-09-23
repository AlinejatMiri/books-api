package edu.ku.bookapi.model;

/**
 * Inbound payload (request body) used when a client sends the values of a book -
 * on {@code PUT} (replace the values of an existing book) and on {@code POST}
 * (describe the book to create).
 *
 * <p>The {@code id} is deliberately <b>not</b> part of this model: the client
 * never chooses an id. On {@code PUT} it identifies the resource in the URL
 * ({@code /api/v3/books/{bookId}}) and must never be changed by a request body;
 * on {@code POST} the server assigns a fresh one. Keeping the client payload
 * separate from the stored resource is what makes "keep the original book ID
 * unchanged" possible.</p>
 *
 * @param title           title of the book
 * @param author          author name
 * @param availableCopies number of available copies
 */
public record BookInput(
        String title,
        String author,
        int availableCopies
) {
}
