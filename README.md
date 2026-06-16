# cv-generator

A Spring Boot 3.5 REST API that manages résumés and exports them as styled A4 PDF files. An optional AI layer — powered by Anthropic Claude via Spring AI — can generate professional summaries and rewrite job descriptions from raw notes. When no API key is configured the app runs normally and the AI endpoints return placeholder responses, so a key is never required to develop or test.

---

## Tech stack

| Component | Technology | Version |
| --- | --- | --- |
| Runtime | Java | 17 |
| Framework | Spring Boot | 3.5.15 |
| Persistence | Spring Data JPA + H2 (in-memory) | managed by Boot |
| Validation | Jakarta Bean Validation 3 | managed by Boot |
| PDF export | Thymeleaf 3 + OpenHTMLtoPDF + PDFBox | OpenHTMLtoPDF 1.0.10 |
| AI integration | Spring AI + Anthropic Claude | Spring AI BOM 1.0.0 |
| Build | Maven (wrapper included) | — |

---

## Prerequisites

- **JDK 17** or later — the only required install. Maven is not required; all commands below use the `mvnw` wrapper included in the repo.

---

## Running the app

```bash
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

The server starts on **<http://localhost:8080>**.

### H2 web console

The in-memory database is accessible at **<http://localhost:8080/h2-console>** while the app is running.

| Field | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:cvdb` |
| User name | `sa` |
| Password | *(leave blank)* |

> Data is lost when the app stops (`ddl-auto=create-drop`).

---

## AI configuration

The two AI endpoints need an Anthropic API key. Set it in the **same terminal session** that starts the app — never put a literal key in source files or properties.

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
./mvnw test
```

`PdfRendererSmokeTest` verifies that the renderer produces at least one page and more than 3 KB, catching the zero-content PDF failure mode. `CvGeneratorApplicationTests` verifies the full application context loads (including Spring AI auto-configuration) without an Anthropic key set.

---

## License

MIT — see [LICENSE](LICENSE). Copyright © 2026 Rachdi Mohamed Amine.
