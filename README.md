# JobTracker API

[![CI](https://github.com/toshkatta/JobTracker/actions/workflows/ci.yml/badge.svg)](https://github.com/toshkatta/JobTracker/actions/workflows/ci.yml)

A REST API for tracking job applications: companies, positions, statuses, salaries and interview notes. Each user only ever sees their own data. Built with Java and Spring Boot as a backend portfolio project.

## Features

- **Authentication**: registration and login with BCrypt-hashed passwords, short-lived JWT access tokens and rotating refresh tokens with theft detection (see [Design decisions](#design-decisions))
- **Job applications**: full CRUD with filtering by status, sorting and pagination
- **Notes**: notes attached to each application, with an "important" flag
- **Ownership**: every query is scoped to the logged-in user; another user's data looks like it doesn't exist
- **Validation and errors**: Bean Validation with consistent [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `application/problem+json` error responses
- **Database migrations** with Flyway, UUIDv7 primary keys
- **Tests**: unit tests plus integration tests against a real PostgreSQL started by Testcontainers

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security) |
| Database | PostgreSQL 18, Hibernate, Flyway |
| Auth | Spring Security OAuth2 resource server (JWT, HS256) |
| Tests | JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers, JaCoCo |
| Build | Maven (wrapper included) |

## Getting started

### Prerequisites

- JDK 25
- Docker (for the local database and for running the tests)

### Run it

```bash
git clone https://github.com/toshkatta/JobTracker.git
cd JobTracker

# 1. configuration: create your own .env and set a real secret
cp .env.example .env
openssl rand -base64 32        # paste the output as JWT_SECRET in .env

# 2. start PostgreSQL (port 5433)
docker compose up -d

# 3. start the API on http://localhost:8081
./mvnw spring-boot:run
```

Flyway creates the schema automatically on startup. The credentials in `docker-compose.yaml` and `application.properties` are for local development only.

### Try it

```bash
# register and log in
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"me@example.com","password":"a-long-password"}'

TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"me@example.com","password":"a-long-password"}' | jq -r .accessToken)

# create and list applications
curl -X POST http://localhost:8081/api/applications \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"company":"Acme","position":"Backend Developer","status":"APPLIED","salary":5000}'

curl "http://localhost:8081/api/applications?status=APPLIED&sort=salary,desc&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### Run the tests

```bash
./mvnw test
```

Docker must be running: the integration tests start a throwaway PostgreSQL container and never touch your development database. A coverage report is written to `target/site/jacoco/index.html`.

## API overview

All routes are under `/api`. Everything except `/health` and `/auth/*` needs an `Authorization: Bearer <access token>` header.

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Liveness check |
| POST | `/auth/register` | Create an account (`409` if the email is taken) |
| POST | `/auth/login` | Returns an access token and a refresh token |
| POST | `/auth/refresh` | Exchanges a refresh token for a new pair |
| POST | `/auth/logout` | Revokes the refresh token's whole family |
| GET | `/applications` | List your applications. Query: `status`, `page`, `size` (max 100), `sort` |
| POST | `/applications` | Create an application |
| GET | `/applications/{id}` | Get one |
| PUT | `/applications/{id}` | Replace one |
| DELETE | `/applications/{id}` | Delete one (and its notes) |
| GET | `/applications/{id}/notes` | List notes, oldest first |
| POST | `/applications/{id}/notes` | Add a note |
| DELETE | `/applications/{id}/notes/{noteId}` | Delete a note |

Sortable fields: `id`, `company`, `position`, `status`, `salary`. Statuses: `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`.

Validation failures return `400` with the failing fields:

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/applications",
  "errors": [{ "field": "company", "message": "must not be blank" }]
}
```

## Project structure

```
src/main/java/com/todoratanasov/jobtracker
├── config        security, JWT and web configuration
├── controller    REST controllers and route constants
├── service       business rules and transactions
├── repository    Spring Data JPA repositories
├── entity        JPA entities
├── model         request and response DTOs
├── error         exceptions and the global exception handler
└── security      helpers for the authenticated user
src/main/resources/db/migration    Flyway migrations
```

Layered architecture: controllers handle HTTP only, services hold the rules, repositories talk to the database. Entities never leave the service layer; the API returns DTO records.

## Design decisions

- **Ownership returns 404, not 403.** Asking for another user's application is indistinguishable from asking for one that doesn't exist, so ids can't be probed. Every repository query includes the owner.
- **Refresh token rotation with reuse detection.** Refresh tokens are random 256-bit values stored only as SHA-256 hashes. Each is single-use. Presenting an already-used token revokes the whole token family, because it signals theft. Logout revokes the family.
- **Login does not reveal whether an email exists.** Wrong password and unknown email return identical responses, and an unknown email still performs a BCrypt comparison so response times match.
- **Stateless access tokens.** Access tokens live 15 minutes and are validated without a database lookup.
- **UUIDv7 primary keys** are not guessable and, being time-ordered, keep database indexes efficient.
- **Sorting is whitelisted** to the fields of the response DTO, so clients can't sort by arbitrary entity fields.
- **Flyway owns the schema.** Hibernate only validates it (`ddl-auto=validate`).
- **Tests treat the API as a black box.** Integration tests send real requests through the full security filter chain to a real database. Test DTOs are deliberately separate from the production ones, so renaming a JSON field breaks a test.

## Known limitations

- No rate limiting. Combined with the `409` on registration, email enumeration is possible. A production system would add rate limiting or an email-verification flow.
- A refresh token used twice at exactly the same moment could succeed twice. Fixing it needs an atomic "revoke if not revoked" update.
- Expired refresh tokens are not cleaned up yet.
- Access tokens cannot be revoked before they expire (at most 15 minutes).
- Security-filter errors (missing or invalid token) return an empty `401` body, not `problem+json`.
- No roles or companies yet; every account is a regular user.

## Roadmap

- [ ] OpenAPI / Swagger UI documentation
- [ ] Dockerfile and a single-command `docker compose` for the whole stack
- [ ] GitHub Actions CI
- [ ] Frontend client
- [ ] Companies and roles
