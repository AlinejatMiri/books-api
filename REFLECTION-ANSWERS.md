# Reflection / Notes — LAB-03 (Updating and Deleting REST Resources)

Course: Enterprise Web Application Development — Kabul University, Faculty of Computer Science
Topic: `PUT` and `DELETE` with Spring Boot

---

### 1. Why is the URL `PUT /api/v3/books/2` and not `/updateBook`?

A REST URL names a **resource** (the book with id 2), not an action. The HTTP
method carries the action: `GET` reads it, `PUT` replaces it, `DELETE` removes
it. So `/api/v3/books/2` is used with three different verbs instead of inventing
verbs in the path (`/updateBook`, `/deleteBook`). This keeps one clear URL per
resource and lets HTTP caches, proxies and monitoring tools understand the API.

### 2. Why is the id taken from the URL and not from the request body?

The id identifies the resource being modified, so it belongs to the URL
(`@PathVariable`). `BookInput` therefore has **no** `id` field, and the handler
always builds the updated book with the id from the path:

```java
Book updatedBook = new Book(bookId, input.title(), input.author(), input.availableCopies());
```

If the body were allowed to supply the id, a client could accidentally (or
maliciously) rename/overwrite another book — and the lab requirement "keep the
original book ID unchanged" could not be guaranteed.

### 3. Why does the update create a new object instead of modifying the old one?

`Book` is a Java **record**, and records are immutable: there is no
`setTitle(...)`. Instead the list element is *replaced*:

```java
books.set(books.indexOf(existingBook.get()), updatedBook);
```

Immutability makes the objects safe to share between threads (a value can never
change while another request is reading it) and makes `equals` / `hashCode`
behave predictably, which is exactly what `indexOf`, `remove` and
`List.addAll` rely on. The trade-off is that a PUT allocates one new object —
irrelevant at this scale, and if the data lived in a database (Hibernate) the
service layer would mutate the managed entity instead.

### 4. Why `200 OK` for PUT but `204 No Content` for DELETE?

- `PUT` succeeds by producing a **result the client wants**: the updated book.
  The lab says "return the updated book with 200 OK", so the body is sent.
- `DELETE` succeeds by removing something. There is nothing meaningful left to
  describe, so `204 No Content` says "done, and there is deliberately no body".
  Returning `200` with a body ("deleted!") is a common mistake, and some
  `200`-with-empty-body responses confuse clients about whether the operation
  really happened.

### 5. Why `404 Not Found` and not a server error?

`BookNotFoundException` never happens in the current code, because the
controller checks the result of `findById(...)` and answers with
`ResponseEntity.notFound()`. The id is a *valid* request to a *non-existing*
resource, which is precisely what `404` means (the same code is returned for
`GET /api/v3/books/99`). `500` would be wrong: nothing failed on the server.
Notice also that a failed `PUT`/`DELETE` must leave the catalogue unchanged,
which is why the `404` branch returns **before** any list modification.

### 6. Are PUT and DELETE idempotent?

Yes, and that matters:

| Request | 1st call | 2nd call | Idempotent? |
|---|---|---|---|
| `PUT /books/2` (same body) | 200, book 2 updated | 200, book 2 in the same state | ✅ same end state |
| `DELETE /books/3` | 204, book 3 gone | 404, book 3 still gone | ✅ same end state |

"Idempotent" means the *server state* after N calls equals the state after one
call — not that the status code is identical. That is why the second `DELETE`
may answer `404` while the operation is still considered idempotent. Retrying a
safe method (`GET`) or an idempotent one (`PUT`, `DELETE`) is therefore safe;
`POST` is not.

### 7. Why a `CopyOnWriteArrayList` in the controller?

Tomcat handles several requests in parallel on different threads. A plain
`ArrayList` being read by `GET` while a `PUT`/`DELETE` mutates it can throw
`ConcurrentModificationException` or return half-updated data (the original code
used the classic stream-over-list pattern together with `List.set`, which does
not support concurrent modification). `CopyOnWriteArrayList` writes by copying
the backing array, so readers always see a consistent snapshot — ideal for a
small, read-mostly list like this catalogue.

### 8. What are the limits of this in-memory design?

- Data is **lost on restart** (the three initial books reappear) and cannot be
  shared by two instances.
- There is no concurrency control: two simultaneous `PUT`s on the same id
  "last write wins".
- The list grows unbounded, and there is no persistence, validation, or
  pagination.

That is acceptable for this lab (the hand-out explicitly asks for an in-memory
list); in a real application the same controller would delegate to a service and
a database repository. The previous version of this repository (tag
`lab-01-complete` / branch `lab-01-advanced`) shows exactly that evolution:
JPA + H2, DTO mapping, validation and a service layer, with the same REST
contract.

### 9. Postman results observed (live run)

| # | Method | Endpoint | Expected | Observed |
|---|---|---|---|---|
| 1 | GET | `/api/v3/books` | 200 OK, 3 books | ✅ 3 books |
| 2 | PUT | `/api/v3/books/2` | 200 OK + updated book | ✅ id 2, "Modern Web Development", 6 copies |
| 3 | PUT | `/api/v3/books/99` | 404 Not Found | ✅ 404 |
| 4 | DELETE | `/api/v3/books/3` | 204 No Content | ✅ 204, empty body |
| 5 | DELETE | `/api/v3/books/99` | 404 Not Found | ✅ 404 |
| 6 | GET | `/api/v3/books` | 200 OK, 2 books, book 2 updated | ✅ 2 books, ids 1 and 2 |
