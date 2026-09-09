# Book Catalogue REST API — `book-api`

A production-ready Spring Boot REST API for a library catalogue, built as the
LAB-01 deliverable for *Enterprise Web Application Development* (Kabul
University, Department of Information Systems).

```
Base URL:  http://localhost:8080/api/v1/books
Swagger:   http://localhost:8080/swagger-ui.html
OpenAPI:   http://localhost:8080/v3/api-docs
Health:    http://localhost:8080/actuator/health
H2 console:http://localhost:8080/h2-console   (dev profile only)
```

---

## Features

| Area | What you get |
|---|---|
| CRUD | `GET`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}` on `/api/v1/books` |
| Pagination | `?page=0&size=20&sort=title,asc` with a stable metadata envelope (`PageResponse`) |
| Validation | Jakarta Bean Validation on DTOs + custom `@Isbn` (13 digits **and** GS1 check digit) and `@PublishedYear` (1900 … current year) |
| Errors | Uniform `ErrorResponse` JSON (timestamp, status, error, message, path, validationErrors) via `@RestControllerAdvice` |
| DTO pattern | `BookRequest` / `BookResponse`, mapped with **MapStruct** — entities never leave the service layer |
| Caching | Caffeine caches (`books` for pages, `book` for single reads), evicted/updated on every mutation |
| Docs | OpenAPI 3 (springdoc) — every endpoint, parameter and response code documented |
| Monitoring | Spring Boot Actuator: `health`, `info`, `metrics`, `caches` |
| Security extras | CORS whitelist for localhost dev clients; optional fixed-window rate limiter (`book-api.rate-limit.enabled`) |
| Seeding | 5 demo books with **valid ISBN-13 check digits** via `ApplicationRunner` (idempotent) |
| Profiles | `application.yml` (safe defaults) + `dev` + `prod` + `test` |
| Tests | 62 tests: service unit tests (Mockito), `@DataJpaTest`, `@WebMvcTest`, handler & filter unit tests, and full E2E tests over real HTTP |
| Coverage | JaCoCo line-coverage gate of 80 % — **measured: 91 %** |

## Tech stack

| Component | Version / choice |
|---|---|
| Java | 21 (`--release 21`; builds also on newer local JDKs — Lombok/ByteBuddy pinned) |
| Spring Boot | 3.5.16 (latest 3.x stable) |
| Build tool | Maven 3.9 (wrapper included — no global install needed) |
| Database | H2 in-memory (dev) / file-based H2 (prod, URL overridable) |
| ORM | Spring Data JPA + Hibernate, JPA auditing for `createdAt`/`updatedAt` |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| API docs | springdoc-openapi 2.9.1 (Swagger UI + OpenAPI JSON) |
| Mapping | MapStruct 1.6.3 (+ Lombok 1.18.48) |
| Caching | Spring Cache + Caffeine |
| Testing | JUnit 5, Mockito, AssertJ, Spring Test (`@DataJpaTest`, `@WebMvcTest`, `@SpringBootTest`) |
| Coverage | JaCoCo 0.8.13 with a `verify`-phase 80 % gate |

## Project structure

```
book-api/
├── pom.xml                                   # Spring Boot 3.5.16 / Java 21 + JaCoCo gate
├── Dockerfile, .dockerignore                 # multi-stage container build
├── docs/
│   ├── openapi.yaml                          # static OpenAPI 3 snapshot
│   └── book-api.postman_collection.json      # importable Postman collection
└── src
    ├── main/java/edu/ku/bookapi/
    │   ├── BookApiApplication.java           # entry point (component-scan root)
    │   ├── controller/BookController.java    # REST layer + OpenAPI annotations
    │   ├── service/                          # BookService + BookServiceImpl (caching)
    │   ├── repository/BookRepository.java    # Spring Data JPA + derived queries
    │   ├── model/                            # BaseEntity (id + auditing), Book
    │   ├── dto/                              # BookRequest, BookResponse, PageResponse
    │   ├── mapper/BookMapper.java            # MapStruct entity<->DTO mapping
    │   ├── validation/                       # @Isbn + @PublishedYear custom validators
    │   ├── exception/                        # BookNotFound, DuplicateIsbn, InvalidSort,
    │   │                                     # ErrorResponse, GlobalExceptionHandler
    │   ├── config/                           # ApplicationConfig, PersistenceConfig,
    │   │                                     # OpenApiConfig, BookProperties, DataSeeder
    │   └── filter/RateLimitFilter.java       # optional per-client rate limiting
    ├── main/resources/
    │   ├── application.yml                   # safe defaults (H2, seeding on)
    │   ├── application-dev.yml               # H2 console, SQL logging, DEBUG
    │   ├── application-prod.yml              # env-var datasource, rate limit on
    │   └── application-test.yml              # fresh H2 for tests
    └── test/java/edu/ku/bookapi/
        ├── service/BookServiceImplTest.java          # Mockito unit tests
        ├── repository/BookRepositoryTest.java        # @DataJpaTest integration
        ├── controller/BookControllerTest.java        # @WebMvcTest slice
        ├── exception/GlobalExceptionHandlerTest.java # handler unit tests
        ├── filter/RateLimitFilterTest.java           # rate limiter unit tests
        └── BookApiEndToEndTest.java                  # E2E over real HTTP
```

## Getting started

### Prerequisites
- JDK 21+ on the PATH (the project compiles with `--release 21`; Lombok and
  ByteBuddy are pinned to versions that also allow building on newer local
  JDKs such as 26)
- No global Maven needed — the wrapper (`./mvnw`) is included
- Docker only if you want the container build (optional)

### Build & test
```bash
./mvnw clean verify          # compile + all tests + JaCoCo 80% gate + report
./mvnw jacoco:report         # regenerate the HTML report alone
```
Coverage report: `target/site/jacoco/index.html` (line coverage ≈ 91 %).

### Run
```bash
./mvnw spring-boot:run                              # dev profile (default)
java -jar target/book-api-0.0.1-SNAPSHOT.jar        # dev profile
SPRING_PROFILES_ACTIVE=prod java -jar target/book-api-0.0.1-SNAPSHOT.jar
```
When no profile is requested the app falls back to `dev`
(`spring.profiles.default: dev`).

| Profile | Database | Notes |
|---|---|---|
| *(default)/dev* | in-memory H2, auto-created schema | seeding on, H2 console on, SQL + DEBUG logging |
| *prod* | H2 file DB (URL via env var), schema update | seeding off, rate limiting on, WARN logs |
| *test* | fresh in-memory H2 per context | used automatically by the test suite |

### Seeded sample data
The `DataSeeder` inserts these books on an empty database (all ISBNs have
valid GS1 check digits): Clean Code (2008), Effective Java (2018),
Designing Data-Intensive Applications (2017), Spring in Action (2022),
Computer Networks (2010).

---

## API reference

| Method | Path | Success | Description |
|---|---|---|---|
| GET | `/api/v1/books` | 200 | Paginated, sortable list (`?page=&size=&sort=`) |
| GET | `/api/v1/books/{id}` | 200 | Single book by id |
| POST | `/api/v1/books` | 201 + `Location` | Create a book |
| PUT | `/api/v1/books/{id}` | 200 | Update an existing book |
| DELETE | `/api/v1/books/{id}` | 204 | Delete a book |

### Validation rules (`BookRequest`)

| Field | Rules |
|---|---|
| `title` | required, ≤ 200 chars |
| `author` | required, ≤ 100 chars |
| `isbn` | valid ISBN-13 (13 digits, hyphens/spaces tolerated, check digit verified); normalized before storage |
| `publishedYear` | required, 1900 … current year |
| `category` | required, ≤ 100 chars |

### Examples

**List with pagination** — `GET /api/v1/books?page=0&size=2&sort=id,asc`
```json
{
  "content": [
    {"id":1,"title":"Clean Code","author":"Robert C. Martin","isbn":"9780132350884",
     "publishedYear":2008,"category":"Software Engineering",
     "createdAt":"2026-09-09T16:12:46.85776","updatedAt":"2026-09-09T16:12:46.85776"},
    {"id":2,"title":"Effective Java","author":"Joshua Bloch","isbn":"9780134685991",
     "publishedYear":2018,"category":"Programming",
     "createdAt":"2026-09-09T16:12:46.923802","updatedAt":"2026-09-09T16:12:46.923802"}
  ],
  "page":0,"size":2,"totalElements":5,"totalPages":3,
  "first":true,"last":false,"hasNext":true,"hasPrevious":false,"sort":"id,asc"
}
```

**Create** — `POST /api/v1/books`
```bash
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Refactoring","author":"Martin Fowler","isbn":"978-0-13-475759-9",
       "publishedYear":2018,"category":"Programming"}'
```
Returns **201** with the persisted book (ISBN normalized to `9780134757599`)
and a `Location: /api/v1/books/6` header. Posting an existing ISBN returns
**409 Conflict**.

**Validation failure** — `POST` with `{"title":"","isbn":"123","publishedYear":1800}`
returns **400**:
```json
{
  "timestamp":"2026-09-09T16:12:59.3847056","status":400,"error":"Bad Request",
  "message":"Validation failed. Check the 'validationErrors' field.",
  "path":"/api/v1/books",
  "validationErrors":{
    "title":"Title is required",
    "author":"Author is required",
    "isbn":"ISBN must be a valid 13-digit ISBN-13 (hyphens and spaces are allowed)",
    "publishedYear":"Published year must be between 1900 and the current year",
    "category":"Category is required"
  }
}
```

**Not found** — `GET /api/v1/books/999` → **404**
```json
{"timestamp":"...","status":404,"error":"Not Found",
 "message":"Book not found with id: 999","path":"/api/v1/books/999"}
```

**Delete** — `DELETE /api/v1/books/5` → **204 No Content**; repeating it or
reading id 5 afterwards returns **404**.

Other handled cases: malformed JSON → **400**, non-numeric id → **400**,
unknown sort property → **400**, wrong HTTP verb → **405**, unknown path →
**404**, unexpected failure → **500** (details only in the server log).

---

## Caching

Two Caffeine caches back the service (TTL 5 min, max 1000 entries each —
configurable):

| Cache | Key | Written by | Evicted by |
|---|---|---|---|
| `books` | the request `Pageable` | `GET /api/v1/books` | any create/update/delete |
| `book` | book id | `GET /api/v1/books/{id}` | delete; updated in place by `PUT` (`@CachePut`) |

The E2E suite proves the behaviour: an update is immediately visible on a
cached single read, and creating a book makes the new total appear on a
previously cached page request.

## Configuration

All application-specific settings are bound to `BookProperties`
(`@ConfigurationProperties("book-api")`) with IDE autocompletion metadata
generated by `spring-boot-configuration-processor`:

| Property | Default | Purpose |
|---|---|---|
| `book-api.seed.enabled` | `true` | seed demo data when the catalogue is empty |
| `book-api.cors.allowed-origins` | localhost:3000/4200/5173/8081 | browser origins allowed by CORS |
| `book-api.rate-limit.enabled` | `false` | enable the per-client rate limit filter |
| `book-api.rate-limit.limit` | `100` | requests per client per window |
| `book-api.rate-limit.window-seconds` | `60` | fixed window length |
| `book-api.cache.time-to-live` | `5m` | cache entry TTL |
| `book-api.cache.maximum-size` | `1000` | max entries per cache |

Production datasource is externalized (see `application-prod.yml`):
`BOOK_API_DATASOURCE_URL`, `BOOK_API_DATASOURCE_USERNAME`,
`BOOK_API_DATASOURCE_PASSWORD`.

Spring Boot knobs: `spring.data.web.pageable.max-page-size=100` caps page
size, `spring.jpa.hibernate.ddl-auto=update` auto-creates the schema.

## Monitoring

Actuator endpoints exposed over the web: `health`, `info`, `metrics`,
`caches`. In `prod`, health details are hidden.

## Testing

| Suite | Type | Focus |
|---|---|---|
| `BookServiceImplTest` | Mockito unit | service logic, ISBN normalization, duplicate & not-found paths, page mapping |
| `BookRepositoryTest` | `@DataJpaTest` + real H2 | persistence, derived queries, unique-ISBN constraint, auditing timestamps |
| `BookControllerTest` | `@WebMvcTest` | routing, status codes, pagination binding, validation errors, malformed JSON, non-numeric ids |
| `GlobalExceptionHandlerTest` | unit | every handler branch (400/404/405/409/500, no internal detail leaks) |
| `RateLimitFilterTest` | unit | fixed window, per-client buckets, `X-Forwarded-For`, disabled & non-API paths |
| `BookApiEndToEndTest` | `@SpringBootTest` + real HTTP | the full acceptance list: CRUD, pagination, duplicates, validation, caching behaviour, Swagger, actuator |

```bash
./mvnw test                        # tests + JaCoCo report
./mvnw clean verify                # + 80% coverage gate
```
Latest run: **62 tests, 0 failures** — line coverage **91.03 %**
(instruction 92.15 %), report at `target/site/jacoco/index.html`.

> Note on Testcontainers: the hand-out lists TestContainers in the stack, but
> Docker is not available on this machine, so repository tests run against
> real H2 instead. The test suite is structured so a `@Testcontainers`
> Postgres slice can be added later without touching the other layers.

## Docker

```bash
docker build -t book-api .
docker run -p 8080:8080 book-api          # prod profile, H2 file in /app/data
```
Multi-stage build on `eclipse-temurin:21`, non-root user, `HEALTHCHECK`
against `/actuator/health`.

## Design decisions (brief)

1. **DTO boundary** — controllers accept/return DTOs only; MapStruct
   generates the mapping at compile time (no reflection, no Lombok pitfalls
   thanks to `lombok-mapstruct-binding`).
2. **Validation at the edge, defence in depth** — `BookRequest` is the single
   source for input validation with per-field messages; the entity repeats
   the constraints so Hibernate rejects bad writes even without the web layer.
3. **Custom validators over regex-only** — `@Isbn` verifies the GS1 check
   digit (a "13-digit" pattern alone accepts garbage) and `@PublishedYear`
   compares against the *current* year instead of a hard-coded `@Max(2026)`,
   so the API does not silently break in 2027.
4. **Stable pagination envelope** — Spring's `PageImpl` is never serialized
   directly (unstable JSON across Spring Data versions); `PageResponse`
   owns the contract.
5. **Service-level duplicate checks + DB constraint** — ISBN uniqueness is
   checked in the service for a friendly 409, and backed by a real unique
   constraint; a `DataIntegrityViolationException` handler is the race
   safety net.
6. **Sort whitelist** — unknown `sort=` fields are rejected with 400
   (`InvalidSortPropertyException`) instead of a late 500 from Hibernate.
7. **Cache invalidation strategy** — list cache fully evicted on mutation
   (cheap, correct), single-item cache updated via `@CachePut` on update and
   evicted on delete.
8. **Profiles with safe defaults** — the base `application.yml` alone can
   boot the app (in-memory H2); `dev`/`prod` layer behaviour on top, and
   `prod` reads its datasource from environment variables.
9. **Rate limiting** — a dependency-free fixed-window filter (per client,
   `X-Forwarded-For` aware) that is disabled by default; a real deployment
   would swap in a shared store (e.g. Bucket4j + Redis) for clusters.
10. **Auditing** — `createdAt`/`updatedAt` are set by JPA auditing in
    `BaseEntity` (`@CreatedDate`/`@LastModifiedDate`), never by service code.

## Known limitations / future work

- Basic auth & API keys are not implemented (optional per the hand-out);
  CORS + validation + rate limiting cover the demonstrated security surface.
- Single-node cache & rate limiter — introduce Redis for clustered deploys.
- Testcontainers slice (Postgres) when Docker is available.


