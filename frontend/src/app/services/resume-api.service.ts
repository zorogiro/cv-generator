import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ResumeRequest,
  ResumeResponse,
  SummaryResponse,
  ImprovedDescriptionResponse,
} from '../models/resume.model';

@Injectable({ providedIn: 'root' })
export class ResumeApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/resumes';

  list(): Observable<ResumeResponse[]> {
    return this.http.get<ResumeResponse[]>(this.base);
  }

  get(id: number): Observable<ResumeResponse> {
    return this.http.get<ResumeResponse>(`${this.base}/${id}`);
  }

  create(body: ResumeRequest): Observable<ResumeResponse> {
    return this.http.post<ResumeResponse>(this.base, body);
  }

  update(id: number, body: ResumeRequest): Observable<ResumeResponse> {
    return this.http.put<ResumeResponse>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // responseType: 'blob' is critical — default 'json' corrupts binary PDF bytes.
  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/${id}/pdf`, { responseType: 'blob' });
  }

  generateSummary(id: number, rawInput: string): Observable<SummaryResponse> {
    return this.http.post<SummaryResponse>(
      `${this.base}/${id}/generate-summary`,
      { rawInput }
    );
  }

  improveDescription(
    id: number,
    description: string
  ): Observable<ImprovedDescriptionResponse> {
    return this.http.post<ImprovedDescriptionResponse>(
      `${this.base}/${id}/improve-description`,
      { description }
    );
  }
}
