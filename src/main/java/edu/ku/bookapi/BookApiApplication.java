package edu.ku.bookapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the <b>Book Catalogue REST API</b>.
 *
 * <p>Component scanning starts here and covers every sub-package
 * ({@code controller}, {@code service}, {@code repository}, {@code model},
 * {@code dto}, {@code mapper}, {@code exception}, {@code validation},
 * {@code config}, {@code filter}) - therefore all classes must live under
 * {@code edu.ku.bookapi}.</p>
 */
@SpringBootApplication
public class BookApiApplication {

    /**
     * Bootstraps the Spring application context.
     *
     * @param args command line arguments (e.g. {@code --spring.profiles.active=prod})
     */
    public static void main(String[] args) {
        SpringApplication.run(BookApiApplication.class, args);
    }
}