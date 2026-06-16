# cv-generator

A Spring Boot 3.5 REST API that manages résumés and exports them as styled A4 PDF files. An optional AI layer — powered by Anthropic Claude via Spring AI — can generate professional summaries and rewrite job descriptions from raw notes. When no API key is configured the app runs normally and the AI endpoints return placeholder responses, so a key is never required to develop or test.

---

## Tech stack

| Component | Technology | Version |
| --- | --- | --- |
| Runtime | Java | 17 |
| Framework | Spring Boot | 3.5.15 |
| Persistence | Spring Data JPA + PostgreSQL + Flyway | managed by Boot |
| Validation | Jakarta Bean Validation 3 | managed by Boot |
| PDF export | Thymeleaf 3 + OpenHTMLtoPDF + PDFBox | OpenHTMLtoPDF 1.0.10 |
| AI integration | Spring AI + Anthropic Claude | Spring AI BOM 1.0.0 |
| Build | Maven (wrapper included) | — |

---

## Prerequisites

- **JDK 17** or later
- **Docker** (for the PostgreSQL container) — Maven is not required; all commands use the `mvnw` wrapper included in the repo.

---

## Running the app

### 1. Start PostgreSQL

A `docker-compose.yml` is included. The container uses the same credentials that `application.properties` defaults to, so **no environment variables are needed for local development**:

```bash
docker compose up -d
```

Data is stored in a named Docker volume (`pgdata`) and **persists across container restarts**. To wipe the database: `docker compose down -v`.

### 2. Run the backend

```bash
mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run     # Linux / macOS

# or via Make
make backend-run
```

On first start Flyway runs `V1__init.sql` automatically and creates all tables. You will see a log line like:

```text
Flyway Community Edition ... by Redgate
Successfully applied 1 migration to schema "public"
```

The server starts on **<http://localhost:8080>**.

### Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cvdb` | JDBC URL |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `ANTHROPIC_API_KEY` | *(unset — stub mode)* | Anthropic Claude API key |

Override any variable in the same terminal session that starts the app — never commit credentials to source files.

---

## AI configuration

The two AI endpoints need an Anthropic API key. Set it in the **same terminal session** that starts the app:

```bash
# bash / zsh
export ANTHROPIC_API_KEY=your-key-here
./mvnw spring-boot:run
```

```powershell
# PowerShell
$env:ANTHROPIC_API_KEY = "your-key-here"
mvnw.cmd spring-boot:run
```

**Graceful degradation:** if the variable is unset, the application starts normally. `POST /{id}/generate-summary` and `POST /{id}/improve-description` still respond with HTTP 200, but the body contains a stub message instead of a real AI-generated result. All other endpoints are unaffected.

The configured model is `claude-haiku-4-5-20251001` with a 512-token response cap. Change `spring.ai.anthropic.chat.options.model` in `application.properties` to switch models.

---

## API reference

Base path: `/api/resumes`

| Method | Path | Description | Example request body |
| --- | --- | --- | --- |
| `GET` | `/api/resumes` | List all résumés | — |
| `GET` | `/api/resumes/{id}` | Get one résumé | — |
| `POST` | `/api/resumes` | Create a résumé | see below |
| `PUT` | `/api/resumes/{id}` | Replace a résumé | same shape as POST |
| `DELETE` | `/api/resumes/{id}` | Delete a résumé | — |
| `GET` | `/api/resumes/{id}/pdf` | Download résumé as PDF (`Content-Disposition: attachment`) | — |
| `GET` | `/api/resumes/{id}/preview` | Render the HTML fed to the PDF engine (useful for debugging) | — |
| `POST` | `/api/resumes/{id}/generate-summary` | Generate a professional summary from freeform notes | `{"rawInput":"10 yrs backend dev, led team of 5"}` |
| `POST` | `/api/resumes/{id}/improve-description` | Rewrite a job description to be more impactful | `{"description":"built some microservices"}` |

### Create / update request body

`fullName` and `email` are required. Everything else is optional.

```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+1 555 0001",
  "location": "Tunis, Tunisia",
  "linkedInUrl": "linkedin.com/in/janedoe",
  "githubUrl": "github.com/janedoe",
  "summary": "Backend engineer with 10 years of experience.",
  "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
  "workExperiences": [
    {
      "company": "Acme Corp",
      "title": "Backend Engineer",
      "location": "Remote",
      "startDate": "2020-03-01",
      "endDate": null,
      "description": "Designed and built high-throughput REST APIs."
    }
  ],
  "educations": [
    {
      "institution": "State University",
      "degree": "BSc",
      "fieldOfStudy": "Computer Science",
      "startDate": "2014-09-01",
      "endDate": "2018-06-30"
    }
  ]
}
```

**Notes:**

- `endDate` may be `null` in `workExperiences` or `educations` to indicate a current position / ongoing enrollment.
- `startDate` must be present and not in the future. When `endDate` is provided it must be on or after `startDate` (enforced by `@ValidDateRange`).
- `phone` max 20 chars · `summary` max 2000 chars · `description` max 2000 chars.

### AI endpoint responses

```json
// POST /{id}/generate-summary  →  200
{ "summary": "Results-driven backend engineer with a decade of experience..." }

// POST /{id}/improve-description  →  200
{ "improved": "Designed and delivered a suite of microservices..." }
```

---

## Error responses

All error responses share the same JSON shape:

```json
{
  "timestamp": "2026-06-16T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields failed validation",
  "fields": {
    "email": "must be a well-formed email address",
    "workExperienceDto.dateRange": "end date must not be before start date"
  }
}
```

The `fields` map is present only for validation errors (HTTP 400); it is omitted for all other error types.

| HTTP status | When |
| --- | --- |
| `400 Bad Request` | Bean validation failure (missing required fields, invalid email, bad date range, blank AI input) |
| `404 Not Found` | Resume ID does not exist |
| `502 Bad Gateway` | Anthropic API call failed (network error, auth failure, rate limit) |
| `500 Internal Server Error` | Unexpected server-side error |

---

## Project structure

```text
src/main/java/tn/esprit/cv_generator/
├── entity/          Resume, WorkExperience, Education — JPA entities, timestamps via @PrePersist/@PreUpdate
├── repository/      ResumeRepository — extends JpaRepository<Resume, Long>
├── service/         ResumeService interface + ResumeServiceImpl — CRUD logic
│   ├── pdf/         CvPdfService + CvPdfServiceImpl — Thymeleaf → HTML → PDF via OpenHTMLtoPDF
│   └── ai/          CvAiService interface, CvAiServiceImpl (real), CvAiServiceStub (fallback), CvAiServiceConfig (bean switching)
├── controller/      ResumeController — all REST endpoints
├── dto/             ResumeRequest, ResumeResponse, ErrorResponse, GenerateSummaryRequest,
│                    ImproveDescriptionRequest, SummaryResponse, ImprovedDescriptionResponse
├── config/          AiConfig (ChatClient bean + system prompt), AnthropicKeyPresentCondition,
│                    PdfTemplateConfig (dedicated Thymeleaf engine for PDF rendering)
├── exception/       GlobalExceptionHandler (@RestControllerAdvice), ResourceNotFoundException, AiException
└── validation/      @ValidDateRange (class-level annotation), ValidDateRangeValidator, DateRanged interface

src/main/resources/
├── application.properties
├── fonts/              DejaVuSans.ttf — embedded in every generated PDF
└── templates/
    └── resume.html     Thymeleaf template — A4 portrait, table-based layout, CSS page breaks
```

---

## Running the tests

```bash
mvnw.cmd test   # Windows
./mvnw test     # Linux / macOS
```

No PostgreSQL or environment variables required. The test classpath (`src/test/resources/application.properties`) replaces the datasource with an H2 in-memory database and disables Flyway — Hibernate generates the schema from entities via `create-drop`.

`PdfRendererSmokeTest` verifies that the renderer produces at least one page and more than 3 KB, catching the zero-content PDF failure mode. `CvGeneratorApplicationTests` verifies the full application context loads (including Spring AI auto-configuration) without an Anthropic key set.

---

## License

MIT — see [LICENSE](LICENSE). Copyright © 2026 Rachdi Mohamed Amine.
