package edu.ku.bookapi.model;

import edu.ku.bookapi.validation.Isbn;
import edu.ku.bookapi.validation.PublishedYear;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A book of the library catalogue.
 *
 * <p>Bean-validation annotations are repeated on the entity as a second line of
 * defence ("defense in depth"): the API validates {@code BookRequest} DTOs at the
 * web layer, and Hibernate validates the entity again right before it is written
 * to the database.</p>
 */
@Entity
@Table(
        name = "books",
        uniqueConstraints = @UniqueConstraint(name = "uk_books_isbn", columnNames = "isbn")
)
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Book extends BaseEntity {

    /** Title of the book (max 200 characters). */
    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Author full name (max 100 characters). */
    @NotBlank
    @Size(max = 100)
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    /** ISBN-13, normalized to 13 digits. Unique across the catalogue. */
    @NotBlank
    @Isbn
    @Column(name = "isbn", nullable = false, length = 13)
    private String isbn;

    /** Year the book was published (1900 .. current year). */
    @NotNull
    @PublishedYear
    @Column(name = "published_year", nullable = false)
    private Integer publishedYear;

    /** Category / genre of the book (max 100 characters). */
    @NotBlank
    @Size(max = 100)
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    /**
     * Convenience constructor used by the seeder and tests.
     *
     * @param title         book title
     * @param author        author name
     * @param isbn          13-digit ISBN
     * @param publishedYear publication year
     * @param category      category / genre
     */
    public Book(String title, String author, String isbn, Integer publishedYear, String category) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishedYear = publishedYear;
        this.category = category;
    }
}