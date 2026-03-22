# Spring Redis Blueprint

A minimal Spring Boot microservice template with:

- REST API
- JWT authentication
- PostgreSQL persistence
- Redis caching
- Flyway migrations
- OpenAPI docs
- Unit and integration tests

## Getting Started

To run this project locally, you need:

- Java 25
- Docker + Docker Compose (required for running the application and integration tests)
- Gradle 9.2 (only if you do not use the bundled `./gradlew` wrapper)

## Architecture (Simplified)

This microservice template combines a relational database **(PostgreSQL)** for persistent data with an in-memory
database **(Redis)** for caching.

![Simplified Architecture](assets/architecture_diagram.png)

## Project Structure & Patterns

The template follows a standard layered architecture with a clear separation of concerns, heavily utilizing the
**CQRS (Command Query Responsibility Segregation)** pattern and **DTOs (Data Transfer Objects)**.

### Core Packages

- `controller`: REST API endpoints and routing.
- `service`: Business logic, split into `command` and `query` components.
- `repository`: Spring Data JPA interfaces for database access.
- `model`: Domain models, split into `entity`, `dto`, and `type`.
- `config`: Configuration for Redis, Security, Swagger/OpenAPI, etc.
- `exception`: Global error handling and custom exceptions.
- `security`: JWT filter and authentication logic.

### CQRS Implementation

To separate read and write operations, the service layer is split into Commands and Queries:

- **Command Services (`service.command`)**: Handle write operations (Create, Update, Delete) for entities like `post`,
  `category`, and `tag`. These services are responsible for validating business rules, modifying the state in
  PostgreSQL,
  and triggering cache evictions in Redis to ensure data consistency.
- **Query Services (`service.query`)**: Handle read operations. They are optimized for retrieving data quickly and
  seamlessly integrate with the Redis cache layer to return cached responses whenever possible.

### DTO Pattern & Mappers

This template strictly avoids exposing internal database entities (`model.entity`) directly through the REST
controllers.

- **Data Transfer Objects (`model.dto`)**: Used for all incoming and outgoing API payloads (e.g., `CreatePostRequest`,
  `UpdatePostRequest`).
- **Mappers (`mapper`)**: A mapping layer is responsible for converting between internal Entities and external DTOs,
  ensuring that the API contract remains decoupled from the database schema.

## REST API

The service exposes REST endpoints for managing posts, categories, and tags. Read endpoints are public where applicable,
while write operations are protected with JWT authentication. OpenAPI/Swagger documentation is available when the
application is running.

## Databases and Data Usage

### PostgreSQL (Primary Database)

PostgreSQL is the **source of truth** for application data.

It stores relational entities such as:

- `users`
- `posts`
- `categories`
- `tags`

Usage:

- CRUD and filtered queries are handled through Spring Data JPA repositories.
- Schema changes are versioned and applied with Flyway.

### Redis (Cache Layer)

Redis is used as a **cache layer**, not as a primary datastore.

Usage:

- Post reads are cached (`POST_CACHE`) to reduce database access.
- Cached entries use TTL (10 minutes).
- Post updates/deletes evict cache entries to keep data consistent.

## Test Infrastructure

The project has two layers of automated tests, each with a distinct scope and set of dependencies.

### Unit Tests

Unit tests are written with **JUnit 5** and **Mockito** and cover individual components in isolation. They have no
external dependencies and run entirely in-memory, making them fast and suitable for rapid feedback during development.

### Repository Integration Tests

Repository tests use **`@DataJpaTest`** combined with **Testcontainers** to validate JPA queries and entity mappings
against a real PostgreSQL instance. A single PostgreSQL container (`postgres:16-alpine`) is started once for the entire
test suite via a static initializer in `AbstractRepositoryTest` and shared across all repository test classes,
keeping the suite fast while staying as close to production as possible.

> **Note:** Docker must be running locally for repository integration tests to execute.

### Controller Integration Tests

Controller tests use **`@WebMvcTest`** with **MockMvc** and **`@MockitoBean`** to validate HTTP routing, request
validation, and response serialization at the controller slice. All service dependencies are mocked, so no external
infrastructure is required to run them.

## Run Locally

1. Start infrastructure with Docker Compose:
    - `docker compose up -d`

2. Run the application:
    - `./gradlew bootRun`

## Run Tests

- `./gradlew test`

> **Note:** Docker must be running before executing the full test suite, as repository integration tests
> spin up a PostgreSQL container via Testcontainers.

## API Docs

When the app is running, OpenAPI/Swagger UI is available at:
`http://localhost:8080/swagger-ui/index.html`

## References

- Build a Blog Platform with Spring Security - https://www.youtube.com/watch?v=Gd6AQsthXNY
- Integration Testing with Spring Boot and
  Testcontainers - https://www.blip.pt/blog/posts/integration-testing-with-spring-boot-and-testcontainers/
- Spring Boot Cache with Redis - https://www.baeldung.com/spring-boot-redis-cache
- Using @Autowired and @InjectMocks in Spring Boot Tests - https://www.baeldung.com/spring-test-autowired-injectmocks
