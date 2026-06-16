# CV Generator

A full-stack CV builder: a Spring Boot REST API paired with an Angular single-page app. Users register, log in, and manage their own résumés entirely in isolation. Each résumé can be downloaded as a styled A4 PDF, previewed as HTML, and enriched by an AI layer (Anthropic Claude via Spring AI) that generates professional summaries and rewrites job-description bullet points from raw notes. The AI layer is optional — the app runs fully without an API key and falls back to stub responses on those endpoints.

---

## Stack

| Layer | Technology | Version |
| --- | --- | --- |
| Backend runtime | Java | 17 |
| Backend framework | Spring Boot | 3.5.15 |
| Persistence | PostgreSQL | 16 |
| Schema migrations | Flyway | managed by Spring Boot BOM |
| ORM | Spring Data JPA / Hibernate | managed by Spring Boot BOM |
| Auth | Spring Security + JJWT | JJWT 0.12.6 |
| AI | Spring AI — Anthropic (Claude) | 1.0.0 |
| PDF rendering | OpenHTMLtoPDF + PDFBox | 1.0.10 |
| Frontend framework | Angular | 16.2 |
| Infra | Docker Compose | — |

---

## Running the project

### Option A — Docker Compose (everything in containers)

```bash
docker compose up --build
```

This starts four services:

| Service | URL | Notes |
| --- | --- | --- |
| Frontend (nginx) | http://localhost:4200 | Angular app |
| Backend (Spring Boot) | http://localhost:8080 | REST API |
| pgAdmin | http://localhost:5050 | Login: `admin@example.com` / `admin` |
| PostgreSQL | `localhost:5432` | Internal to compose network |

To enable the AI features, pass your key at start time:

```bash
ANTHROPIC_API_KEY=sk-ant-... docker compose up --build
```

### Option B — Local dev (Postgres in Docker, apps on host)

**1. Start the database only:**

```bash
docker compose up db -d
```

**2. Start the backend:**

```bash
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

The API is available at http://localhost:8080.

**3. Start the frontend (in a separate terminal):**

```bash
cd frontend
npm install        # first time only
ng serve
```

The Angular dev server starts at http://localhost:4200. All `/api` requests are proxied to `http://localhost:8080` via `proxy.conf.json` — no CORS issues during local development.

---

## Environment variables

All variables have safe development defaults. **Override them in production.**

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cvdb` | JDBC URL for PostgreSQL |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | `dev-only-secret-must-be-32-chars-long!!` | HS256 signing secret — must be ≥ 32 bytes. **Replace in production.** |
| `JWT_EXPIRY_MS` | `86400000` | Token lifetime in milliseconds (default: 24 h) |
| `ANTHROPIC_API_KEY` | *(empty)* | Claude API key. When unset, AI endpoints return stub responses; the rest of the app is unaffected. |

> Never commit a real `JWT_SECRET` or `ANTHROPIC_API_KEY` to source control.

---

## Authentication flow

1. **Register** — `POST /api/auth/register` with `email`, `password`, and optional `displayName`. Returns a JWT.
2. **Login** — `POST /api/auth/login` with `email` and `password`. Returns a JWT.
3. **Authenticated requests** — the Angular app stores the token in `localStorage` and attaches it as `Authorization: Bearer <token>` on every `/api` call via an HTTP interceptor.
4. **Ownership** — all resume endpoints are scoped to the authenticated user. Requesting another user's resume returns `404`, not `403`, to avoid leaking resource existence.
5. **Token expiry** — a `401` response clears the stored token and redirects the user to `/login`.

---

## API reference

### Auth (public)

| Method | Path | Body | Response |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | `{ email, password, displayName? }` | `201 { token }` |
| `POST` | `/api/auth/login` | `{ email, password }` | `200 { token }` |

### Resumes (requires `Authorization: Bearer <token>`)

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/resumes` | List the authenticated user's resumes |
| `POST` | `/api/resumes` | Create a resume |
| `GET` | `/api/resumes/{id}` | Get one resume (`404` if not yours) |
| `PUT` | `/api/resumes/{id}` | Replace a resume |
| `DELETE` | `/api/resumes/{id}` | Delete a resume |
| `GET` | `/api/resumes/{id}/pdf` | Download as A4 PDF |
| `GET` | `/api/resumes/{id}/preview` | Render as HTML |
| `POST` | `/api/resumes/{id}/generate-summary` | AI: generate a professional summary |
| `POST` | `/api/resumes/{id}/improve-description` | AI: rewrite a job description |

A Postman collection covering all endpoints is in [`docs/cv-generator.postman_collection.json`](docs/cv-generator.postman_collection.json).

---

## Project structure

```
cv-generator/
├── src/main/java/tn/esprit/cv_generator/
│   ├── config/           # AiConfig, PdfTemplateConfig
│   ├── controller/       # AuthController, ResumeController
│   ├── dto/              # request / response records
│   ├── entity/           # Resume, User, WorkExperience, Education
│   ├── exception/        # GlobalExceptionHandler + custom exceptions
│   ├── repository/       # Spring Data JPA repositories
│   ├── security/         # SecurityConfig, JwtService, JwtAuthenticationFilter
│   ├── service/
│   │   ├── ai/           # CvAiService (Claude integration + stub)
│   │   ├── auth/         # AuthService
│   │   └── pdf/          # CvPdfService (OpenHTMLtoPDF)
│   └── validation/       # @ValidDateRange constraint
├── src/main/resources/
│   ├── db/migration/     # V1__init.sql, V2__add_users.sql (Flyway)
│   └── templates/        # resume.html (Thymeleaf + PDF template)
├── frontend/src/app/
│   ├── login/            # LoginComponent
│   ├── register/         # RegisterComponent
│   ├── resume-list/      # ResumeListComponent
│   ├── resume-form/      # ResumeFormComponent (create + edit)
│   ├── services/         # AuthService, ResumeApiService
│   ├── auth.guard.ts     # CanActivateFn — blocks unauthenticated routes
│   └── auth.interceptor.ts  # Attaches Bearer token; bounces 401 → /login
├── Dockerfile.backend
├── Dockerfile.frontend
├── docker-compose.yml
└── Makefile              # make help for available targets
```

---

## License

MIT
