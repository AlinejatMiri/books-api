package edu.ku.bookapi.controller;

import edu.ku.bookapi.dto.BookRequest;
import edu.ku.bookapi.dto.BookResponse;
import edu.ku.bookapi.dto.PageResponse;
import edu.ku.bookapi.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST controller for the library catalogue under {@code /api/v1/books}.
 *
 * <p>Every operation is documented for Swagger UI via springdoc annotations.
 * Requests/responses are logged at DEBUG level; business events are logged at
 * INFO level by the service layer.</p>
 */
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books", description = "CRUD operations for the library catalogue")
public class BookController {

    private final BookService bookService;

    /**
     * Returns a paginated, sortable list of all books
     * ({@code ?page=0&size=20&sort=title,asc}).
     */
    @GetMapping
    @Operation(summary = "List all books", description = "Returns a paginated and sortable page of the catalogue. "
            + "Parameters: page (0-based), size (max 100), sort (e.g. sort=title,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of books",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameter",
                    content = @Content)
    })
    public PageResponse<BookResponse> getAllBooks(
            @ParameterObject
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination and sorting information")
            Pageable pageable) {
        log.debug("GET /api/v1/books - page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PageResponse<BookResponse> response = bookService.getAll(pageable);
        log.debug("GET /api/v1/books - returning {} of {} books",
                response.content().size(), response.totalElements());
        return response;
    }

    /** Returns a single book by id. */
    @GetMapping("/{id}")
    @Operation(summary = "Get a book by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    public BookResponse getBookById(
            @Parameter(description = "Identifier of the book") @PathVariable Long id) {
        log.debug("GET /api/v1/books/{}", id);
        return bookService.getById(id);
    }

    /** Creates a new book (201 + Location header). */
    @PostMapping
    @Operation(summary = "Create a new book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "409", description = "ISBN already exists", content = @Content)
    })
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        log.debug("POST /api/v1/books - payload={}", request);
        BookResponse created = bookService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        log.debug("POST /api/v1/books - created id={} at {}", created.getId(), location);
        return ResponseEntity.created(location).body(created);
    }

    /** Updates an existing book. */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "ISBN already in use by another book", content = @Content)
    })
    public BookResponse updateBook(
            @Parameter(description = "Identifier of the book") @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        log.debug("PUT /api/v1/books/{} - payload={}", id, request);
        return bookService.update(id, request);
    }

    /** Deletes a book by id (204 No Content). */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Identifier of the book") @PathVariable Long id) {
        log.debug("DELETE /api/v1/books/{}", id);
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}