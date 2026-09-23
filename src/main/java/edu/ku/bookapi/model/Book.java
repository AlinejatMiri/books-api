package edu.ku.bookapi.model;

/**
 * A book of the library catalogue - the REST <em>resource</em> exposed by
 * {@code /api/v1/books}.
 *
 * <p>Implemented as a Java {@code record}: the components are immutable, the
 * compiler generates the constructor, accessors ({@code id()}, {@code title()},
 * ...), {@code equals}, {@code hashCode} and {@code toString}. Because a record
 * cannot be modified in place, an update creates a new instance that keeps the
 * original id and replaces the old instance in the in-memory list.</p>
 *
 * @param id              unique book identifier (assigned by the application)
 * @param title           book title
 * @param author          author name
 * @param availableCopies number of copies currently available on the shelf
 */
public record Book(
        Long id,
        String title,
        String author,
        int availableCopies
) {
}
