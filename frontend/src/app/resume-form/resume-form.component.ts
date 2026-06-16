import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  AbstractControl,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ResumeApiService } from '../services/resume-api.service';
import {
  ResumeRequest,
  ResumeResponse,
  ApiErrorResponse,
} from '../models/resume.model';

@Component({
  selector: 'app-resume-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './resume-form.component.html',
})
export class ResumeFormComponent implements OnInit {
  private readonly fb     = inject(FormBuilder);
  private readonly api    = inject(ResumeApiService);
  private readonly route  = inject(ActivatedRoute);
  private readonly router = inject(Router);

  editId: number | null = null;

  saving         = false;
  summaryLoading = false;
  // one loading flag per work-experience row, indexed by position
  improveLoading: boolean[] = [];

  serverError  = '';
  weGlobalError  = '';   // workExperienceDto.dateRange (no index info from backend)
  eduGlobalError = '';   // educationDto.dateRange

  // ── Root form ──────────────────────────────────────────────────────────────
  form = this.fb.group({
    fullName:    ['', Validators.required],
    email:       ['', [Validators.required, Validators.email]],
    phone:       [''],
    location:    [''],
    linkedInUrl: [''],
    githubUrl:   [''],
    summary:     [''],
    skills:      [''],           // comma-separated; split on submit
    workExperiences: this.fb.array([]),
    educations:      this.fb.array([]),
  });

  // ── Typed FormArray accessors ──────────────────────────────────────────────
  get workExperiences(): FormArray { return this.form.get('workExperiences') as FormArray; }
  get educations():      FormArray { return this.form.get('educations')      as FormArray; }

  get isEdit(): boolean { return this.editId !== null; }

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.editId = +idParam;
      this.api.get(this.editId).subscribe({
        next: (r) => this.patchForm(r),
        error: () => { this.serverError = 'Could not load résumé.'; },
      });
    }
  }

  // ── FormGroup factories ────────────────────────────────────────────────────
  private buildWeGroup(we?: Partial<ResumeResponse['workExperiences'][0]>): FormGroup {
    return this.fb.group({
      company:     [we?.company     ?? '', Validators.required],
      title:       [we?.title       ?? '', Validators.required],
      location:    [we?.location    ?? ''],
      startDate:   [we?.startDate   ?? '', Validators.required],
      endDate:     [we?.endDate     ?? null],
      description: [we?.description ?? ''],
    });
  }

  private buildEduGroup(edu?: Partial<ResumeResponse['educations'][0]>): FormGroup {
    return this.fb.group({
      institution:  [edu?.institution  ?? '', Validators.required],
      degree:       [edu?.degree       ?? '', Validators.required],
      fieldOfStudy: [edu?.fieldOfStudy ?? ''],
      startDate:    [edu?.startDate    ?? '', Validators.required],
      endDate:      [edu?.endDate      ?? null],
    });
  }

  // ── Array mutation ─────────────────────────────────────────────────────────
  addWe(): void {
    this.workExperiences.push(this.buildWeGroup());
    this.improveLoading.push(false);
  }

  removeWe(i: number): void {
    this.workExperiences.removeAt(i);
    this.improveLoading.splice(i, 1);
  }

  addEdu(): void  { this.educations.push(this.buildEduGroup()); }
  removeEdu(i: number): void { this.educations.removeAt(i); }

  // ── Patch form from API response ───────────────────────────────────────────
  private patchForm(r: ResumeResponse): void {
    this.form.patchValue({
      fullName:    r.fullName,
      email:       r.email,
      phone:       r.phone       ?? '',
      location:    r.location    ?? '',
      linkedInUrl: r.linkedInUrl ?? '',
      githubUrl:   r.githubUrl   ?? '',
      summary:     r.summary     ?? '',
      skills:      r.skills?.join(', ') ?? '',
    });

    this.workExperiences.clear();
    this.improveLoading = [];
    (r.workExperiences ?? []).forEach(we => {
      this.workExperiences.push(this.buildWeGroup(we));
      this.improveLoading.push(false);
    });

    this.educations.clear();
    (r.educations ?? []).forEach(edu => this.educations.push(this.buildEduGroup(edu)));
  }

  // ── Build request payload ──────────────────────────────────────────────────
  // Item 3: empty string from <input type="date"> must become null, not "".
  private nullIfEmpty(s: string | null | undefined): string | null {
    return s?.trim() ? s.trim() : null;
  }

  private buildPayload(): ResumeRequest {
    const v = this.form.getRawValue();

    return {
      fullName:    v.fullName!,
      email:       v.email!,
      phone:       this.nullIfEmpty(v.phone),
      location:    this.nullIfEmpty(v.location),
      linkedInUrl: this.nullIfEmpty(v.linkedInUrl),
      githubUrl:   this.nullIfEmpty(v.githubUrl),
      summary:     this.nullIfEmpty(v.summary),
      skills: v.skills?.trim()
        ? v.skills.split(',').map((s: string) => s.trim()).filter(Boolean)
        : [],
      workExperiences: (v.workExperiences as any[]).map(we => ({
        company:     we.company,
        title:       we.title,
        location:    this.nullIfEmpty(we.location),
        startDate:   we.startDate,
        endDate:     this.nullIfEmpty(we.endDate),      // '' → null
        description: this.nullIfEmpty(we.description),
      })),
      educations: (v.educations as any[]).map(edu => ({
        institution:  edu.institution,
        degree:       edu.degree,
        fieldOfStudy: this.nullIfEmpty(edu.fieldOfStudy),
        startDate:    edu.startDate,
        endDate:      this.nullIfEmpty(edu.endDate),    // '' → null
      })),
    };
  }

  // ── Submit ─────────────────────────────────────────────────────────────────
  save(): void {
    this.clearErrors();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const payload = this.buildPayload();
    const call = this.editId
      ? this.api.update(this.editId, payload)
      : this.api.create(payload);

    call.subscribe({
      next: (res) => {
        this.saving = false;
        if (!this.editId) {
          // Item 4: redirect to edit after create so PDF/AI buttons have an id.
          this.router.navigate(['/resumes', res.id, 'edit']);
        }
      },
      error: (err) => {
        this.saving = false;
        this.handleError(err);
      },
    });
  }

  // ── Error handling ─────────────────────────────────────────────────────────
  private handleError(err: any): void {
    const body: ApiErrorResponse = err.error;
    if (err.status === 400 && body?.fields) {
      this.applyFieldErrors(body.fields);
    } else {
      this.serverError = body?.message ?? 'An unexpected error occurred.';
    }
  }

  private applyFieldErrors(fields: Record<string, string>): void {
    // Field error key format from Spring: workExperiences[N].fieldName
    const weRe  = /^workExperiences\[(\d+)\]\.(\w+)$/;
    const eduRe = /^educations\[(\d+)\]\.(\w+)$/;

    for (const [key, msg] of Object.entries(fields)) {
      const weMatch  = weRe.exec(key);
      const eduMatch = eduRe.exec(key);

      if (weMatch) {
        const ctrl = this.workExperiences.at(+weMatch[1])?.get(weMatch[2]);
        ctrl?.setErrors({ backend: msg });
      } else if (eduMatch) {
        const ctrl = this.educations.at(+eduMatch[1])?.get(eduMatch[2]);
        ctrl?.setErrors({ backend: msg });
      } else if (key === 'workExperienceDto.dateRange') {
        // Class-level @ValidDateRange on WorkExperienceDto — index is lost in the key.
        this.weGlobalError = msg;
      } else if (key === 'educationDto.dateRange') {
        this.eduGlobalError = msg;
      } else {
        // Root-level fields: fullName, email, etc.
        this.form.get(key)?.setErrors({ backend: msg });
      }
    }
  }

  private clearErrors(): void {
    this.serverError   = '';
    this.weGlobalError  = '';
    this.eduGlobalError = '';
  }

  // ── AI actions ─────────────────────────────────────────────────────────────
  generateSummary(): void {
    if (!this.editId) return;
    const rawInput = this.form.get('summary')?.value?.trim() ?? '';
    if (!rawInput) {
      this.serverError = 'Type some notes in the summary box first, then click Generate.';
      return;
    }
    this.summaryLoading = true;
    this.api.generateSummary(this.editId, rawInput).subscribe({
      next: (res) => {
        this.form.get('summary')?.setValue(res.summary);
        this.summaryLoading = false;
      },
      error: (err) => {
        this.summaryLoading = false;
        this.serverError = err.status === 502
          ? 'AI service is temporarily unavailable. Try again later.'
          : 'Failed to generate summary.';
      },
    });
  }

  improveDescription(i: number): void {
    if (!this.editId) return;
    const ctrl = this.workExperiences.at(i)?.get('description');
    const text = ctrl?.value?.trim() ?? '';
    if (!text) return;
    this.improveLoading[i] = true;
    this.api.improveDescription(this.editId, text).subscribe({
      next: (res) => {
        ctrl?.setValue(res.improved);
        this.improveLoading[i] = false;
      },
      error: (err) => {
        this.improveLoading[i] = false;
        this.serverError = err.status === 502
          ? 'AI service is temporarily unavailable. Try again later.'
          : 'Failed to improve description.';
      },
    });
  }

  // ── PDF actions (only in edit mode) ───────────────────────────────────────
  downloadPdf(): void {
    if (!this.editId) return;
    this.api.downloadPdf(this.editId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a   = document.createElement('a');
        a.href     = url;
        a.download = `resume-${this.editId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { this.serverError = 'Failed to download PDF.'; },
    });
  }

  preview(): void {
    if (!this.editId) return;
    window.open(`/api/resumes/${this.editId}/preview`, '_blank');
  }

  // ── Template helper ────────────────────────────────────────────────────────
  ctrl(path: string): AbstractControl | null {
    return this.form.get(path);
  }
}
