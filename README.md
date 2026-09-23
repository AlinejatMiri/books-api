# Book Management REST API — `book-api`

**LAB ASSIGNMENT-03 — Updating and Deleting REST Resources**
`PUT` and `DELETE` with Spring Boot · Enterprise Web Applications Development ·
Kabul University, Faculty of Computer Science.

A small library catalogue REST API whose data lives in an **in-memory list**
(no database, as required by the hand-out). Besides `GET`, this deliverable
implements the two new operations:

```
Base URL:   http://localhost:8080/api/v3/books
GET    /api/v3/books            -> 200 OK   (list all books)
PUT    /api/v3/books/{bookId}   -> 200 OK + updated book  |  404 Not Found
DELETE /api/v3/books/{bookId}   -> 204 No Content         |  404 Not Found
POST   /api/v3/books            -> 201 Created + Location  |  (our extension)
```

> **API version note.** The resource is versioned in the URL (`/api/v3/...`). That
> prefix is declared exactly once, in the class-level
> `@RequestMapping("/api/v3/books")` of `BookController`; all four endpoints
> inherit it, so moving to another version means editing that single line.
> Anything else — tests, Postman collection, `openapi.yaml`, Docker health check —
> has already been aligned to `v3`.

> **`POST` is an extension.** The LAB-03 hand-out requires `GET`, `PUT` and
> `DELETE` only, and those three behave exactly as the assignment specifies.
> `POST /api/v3/books` was added on top because a catalogue resource normally
> supports the full set of REST verbs, and because it makes the API testable from
> an empty start. If your instructor grades strictly against the hand-out, the
> three required endpoints are unaffected by it.

## Initial data (loaded at start-up)

```json
[
  { "id": 1, "title": "Java Programming", "author": "John Smith", "availableCopies": 5 },
  { "id": 2, "title": "Web Development",  "author": "Sara Ahmad", "availableCopies": 3 },
  { "id": 3, "title": "Database Systems", "author": "Ali Khan",   "availableCopies": 4 }
]
```

Because the list lives in memory, restarting the application restores these
three books.

## Run the application

```bash
./mvnw spring-boot:run                       # start on http://localhost:8080
./mvnw clean package && java -jar target/book-api-0.0.1-SNAPSHOT.jar
```

Then open Postman and send the requests below (or import
`docs/book-api.postman_collection.json`, set `baseUrl` to
`http://localhost:8080` and press **Send** on every request in order).

## Postman tests (hand-out table)

| # | Method | Endpoint | Body | Expected result | Observed |
|---|---|---|---|---|---|
| 1 | `GET` | `/api/v3/books` | – | `200 OK`, 3 books | ✅ 3 books |
| 2 | `PUT` | `/api/v3/books/2` | `{"title":"Modern Web Development","author":"Sara Ahmad","availableCopies":6}` | `200 OK` + updated book | ✅ id 2, new title, 6 copies |
| 3 | `GET` | `/api/v3/books` | – | `200 OK` + changed book 2 | ✅ book 2 updated |
| 4 | `PUT` | `/api/v3/books/99` | any book JSON | `404 Not Found` | ✅ 404 |
| 5 | `DELETE` | `/api/v3/books/3` | – | `204 No Content` | ✅ 204, no body |
| 6 | `DELETE` | `/api/v3/books/99` | – | `404 Not Found` | ✅ 404 |
| 7 | `GET` | `/api/v3/books` | – | `200 OK`, 2 books left | ✅ ids 1 and 2 |

Postman settings for the PUT request: **Body → raw → JSON** (Postman also sets
`Content-Type: application/json` automatically).

### POST extension (requests 8–12 in the collection)

| # | Method | Endpoint | Body | Expected result | Observed |
|---|---|---|---|---|---|
| 8 | `POST` | `/api/v3/books` | `{"title":"Spring in Action","author":"Craig Walls","availableCopies":2}` | `201 Created` + new book + `Location` header | ✅ id 4, `Location: …/api/v3/books/4` |
| 9 | `GET` | `/api/v3/books` | – | `200 OK`, 3 books, new one listed | ✅ ids 1, 2, 4 |
| 10 | `PUT` | `/api/v3/books/4` | updated title/copies | `200 OK` + updated book | ✅ same id, new values |
| 11 | `DELETE` | `/api/v3/books/4` | – | `204 No Content` | ✅ 204, no body |
| 12 | `GET` | `/api/v3/books` | – | `200 OK`, back to 2 books | ✅ ids 1 and 2 |

Requests 8–12 leave the catalogue exactly where request 7 left it, so the
collection has no side effects on the LAB-03 result.

## Project structure

```
book-api/
├── pom.xml                                  # Spring Boot 3.5.16, Java 21 (web + test only)
├── Dockerfile                               # optional multi-stage container build
├── docs/
│   ├── book-api.postman_collection.json     # importable Postman collection with test scripts
│   └── openapi.yaml                         # OpenAPI 3 snapshot of GET / PUT / DELETE
├── REFLECTION-ANSWERS.md                    # LAB-03 reflection notes
└── src
    ├── main/java/edu/ku/bookapi/
    │   ├── BookApiApplication.java          # entry point (@SpringBootApplication)
    │   ├── controller/BookController.java   # in-memory catalogue + GET / POST / PUT / DELETE
    │   └── model/
    │       ├── Book.java                    # resource record (id, title, author, availableCopies)
    │       └── BookInput.java               # request body record (no id!)
    ├── main/resources/application.yml       # port 8080, logging
    └── test/java/edu/ku/bookapi/
        ├── controller/BookControllerTest.java   # MockMvc slice: 200/201/204/404 per scenario
        └── BookApiEndToEndTest.java             # real HTTP (RANDOM_PORT) in Postman order
```

## Key source code

`model/Book.java` — the REST resource as a Java record (immutable):

```java
public record Book(Long id, String title, String author, int availableCopies) { }
```

`model/BookInput.java` — the request body; note there is **no id**, because the
id belongs to the URL and must never be changed by a client:

```java
public record BookInput(String title, String author, int availableCopies) { }
```

`controller/BookController.java` — in-memory catalogue plus the two new
endpoints (the `// 1 … // 5` comments match the hand-out skeleton):

```java
private final List<Book> books = new CopyOnWriteArrayList<>(List.of(
        new Book(1L, "Java Programming", "John Smith", 5),
        new Book(2L, "Web Development", "Sara Ahmad", 3),
        new Book(3L, "Database Systems", "Ali Khan", 4)));

@PutMapping("/{bookId}")
public ResponseEntity<?> updateBook(@PathVariable Long bookId, @RequestBody BookInput input) {
    Optional<Book> existingBook = findById(bookId);                       // 1. search
    if (existingBook.isEmpty()) return ResponseEntity.notFound().build(); // 5. 404
    Book updatedBook = new Book(bookId, input.title(),                    // 2. new object,
                                input.author(), input.availableCopies()); //    id from the URL
    books.set(books.indexOf(existingBook.get()), updatedBook);            // 3. replace
    return ResponseEntity.ok(updatedBook);                                // 4. 200 OK
}

@DeleteMapping("/{bookId}")
public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
    Optional<Book> existingBook = findById(bookId);                       // 1. search
    if (existingBook.isEmpty()) return ResponseEntity.notFound().build(); // 4. 404
    books.remove(existingBook.get());                                     // 2. remove
    return ResponseEntity.noContent().build();                            // 3. 204
}

@PostMapping                                                              // our extension
public ResponseEntity<Book> createBook(@RequestBody BookInput input) {
    Book createdBook = new Book(nextId.getAndIncrement(),                 // id from the server,
                                input.title(), input.author(),            // never from the body
                                input.availableCopies());
    books.add(createdBook);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()       // Location:
            .path("/{bookId}").buildAndExpand(createdBook.id()).toUri();  //   .../books/4
    return ResponseEntity.created(location).body(createdBook);            // 201 Created
}
```

## Completion checklist

| Requirement | Done |
|---|---|
| `GET` displays all books | ✅ |
| `PUT` updates an existing book (`200 OK` + updated book) | ✅ |
| `PUT` returns `404` for a missing book | ✅ |
| `PUT` keeps the original book id | ✅ |
| `DELETE` removes an existing book (`204 No Content`) | ✅ |
| `DELETE` returns `404` for a missing book | ✅ |
| List is really changed after PUT / DELETE (verified by `GET`) | ✅ |
| Postman tests completed | ✅ collection in `docs/` |
| *(extension)* `POST` creates a book (`201 Created` + `Location`) | ✅ |

## Automated tests

```bash
./mvnw test
```

* `controller/BookControllerTest` — `@WebMvcTest` + `MockMvc`: initial list,
  PUT `200`, PUT `404`, DELETE `204`, DELETE `404`, delete-twice, plus three
  `POST` tests (create → `201` + `Location`, incrementing ids, and
  create → update → delete round trip).
* `BookApiEndToEndTest` — `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate`:
  the same calls as the Postman table over **real HTTP**, including the final
  `GET` that proves the catalogue changed and the `POST` extension.

Both classes rebuild the controller for every test method
(`@DirtiesContext` / ordered scenario), so the in-memory list always starts with
the three original books. Current result: **18 tests, 0 failures**.

## Design notes

1. **Resource-oriented URLs** — `/api/v3/books/{bookId}` with the verb in the
   HTTP method, not `/updateBook` or `/deleteBook`.
2. **Id from the URL, never from the body** — `BookInput` has no `id` field and
   the updated record is built with the `@PathVariable` value, which is what
   makes "keep the original book ID unchanged" true by construction.
3. **`200 OK` vs `204 No Content`** — a successful `PUT` returns the updated
   representation; a successful `DELETE` has nothing to return, so it answers
   `204` with an empty body.
4. **`404 Not Found` for unknown ids** — checked before any mutation, so a
   failed request leaves the catalogue untouched. The status comes from
   `ResponseEntity.notFound()`, not from an exception, which keeps the simple
   in-memory controller readable.
5. **Immutable records** — `Book` cannot be modified in place, so the updated
   instance replaces the old element (`List.set`) at the same position.
6. **Thread safety** — `CopyOnWriteArrayList` is used because Tomcat handles
   concurrent requests; reads always see a consistent snapshot.
7. **No database** — the hand-out asks for an in-memory list, so the whole
   application is web-only (no JPA/H2, no extra configuration).
8. **`POST` answers `201 Created` + `Location`** — the REST convention for a
   creation: the client learns both the new representation and the URL where it
   now lives. The header is built with `ServletUriComponentsBuilder` from the
   incoming request, so the controller does not hard-code host or base path.
9. **Server-generated ids** — `nextId` is an `AtomicLong` starting at `4`
   (right after the three seeded books) and only moves forward, so the id of a
   deleted book is never handed out again while the application runs.

## Known limitations

* The catalogue is lost on restart (by design for this lab) and cannot be shared
  between instances.
* No request validation (e.g. a negative `availableCopies` or a blank title is
  accepted) — outside the scope of LAB-03.
* `POST` is an extension of ours, not a hand-out requirement: it has no
  duplicate-title check, so the same book can be created several times.
* Concurrent `PUT`s on the same book follow "last write wins".

## Note on git history

The earlier, database-backed version of this project (LAB-01: JPA + H2, DTO
mapping, validation, pagination, OpenAPI) is preserved in git — tag
`lab-01-complete`, branch `lab-01-advanced` — in case you need to refer back to
it. The current `main` branch is the LAB-03 deliverable.

