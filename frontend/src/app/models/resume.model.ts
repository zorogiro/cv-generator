// ── Response types (from backend ResumeResponse) ───────────────────────────
// LocalDate serialises as 'yyyy-MM-dd'; LocalDateTime as ISO-8601 string.

export interface WorkExperienceResponse {
  id: number;
  company: string;
  title: string;
  location: string | null;
  startDate: string;       // 'yyyy-MM-dd'
  endDate: string | null;  // null = current position
  description: string | null;
}

export interface EducationResponse {
  id: number;
  institution: string;
  degree: string;
  fieldOfStudy: string | null;
  startDate: string;
  endDate: string | null;
}

export interface ResumeResponse {
  id: number;
  fullName: string;
  email: string;
  phone: string | null;
  location: string | null;
  linkedInUrl: string | null;
  githubUrl: string | null;
  summary: string | null;
  skills: string[];
  workExperiences: WorkExperienceResponse[];
  educations: EducationResponse[];
  createdAt: string;
  updatedAt: string;
}

// ── Request types (mirror ResumeRequest / nested DTOs) ─────────────────────

export interface WorkExperienceRequest {
  company: string;
  title: string;
  location?: string | null;
  startDate: string;
  endDate?: string | null;
  description?: string | null;
}

export interface EducationRequest {
  institution: string;
  degree: string;
  fieldOfStudy?: string | null;
  startDate: string;
  endDate?: string | null;
}

export interface ResumeRequest {
  fullName: string;
  email: string;
  phone?: string | null;
  location?: string | null;
  linkedInUrl?: string | null;
  githubUrl?: string | null;
  summary?: string | null;
  skills?: string[];
  workExperiences?: WorkExperienceRequest[];
  educations?: EducationRequest[];
}

// ── AI DTOs ────────────────────────────────────────────────────────────────

export interface SummaryResponse {
  summary: string;
}

export interface ImprovedDescriptionResponse {
  improved: string;
}

// ── Error response (from GlobalExceptionHandler) ───────────────────────────
// fields is @JsonInclude(NON_NULL) — absent on non-validation errors.

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  fields?: Record<string, string>;
}
