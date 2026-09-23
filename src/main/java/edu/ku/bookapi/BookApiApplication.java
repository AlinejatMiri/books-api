package edu.ku.bookapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the <b>Book Catalogue REST API</b>.
 *
 * <p>LAB-03 - Library book management with HTTP PUT and DELETE. Component
 * scanning starts here and covers every sub-package ({@code controller},
 * {@code model}), therefore all classes must live under
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