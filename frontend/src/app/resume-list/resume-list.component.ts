import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ResumeApiService } from '../services/resume-api.service';
import { ResumeResponse } from '../models/resume.model';

@Component({
  selector: 'app-resume-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './resume-list.component.html',
})
export class ResumeListComponent implements OnInit {
  private readonly api = inject(ResumeApiService);

  resumes: ResumeResponse[] = [];
  loading = false;
  error = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.api.list().subscribe({
      next: (data) => { this.resumes = data; this.loading = false; },
      error: () => { this.error = 'Failed to load résumés.'; this.loading = false; },
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this résumé? This cannot be undone.')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(),
      error: () => { this.error = 'Failed to delete résumé.'; },
    });
  }

  downloadPdf(id: number): void {
    this.api.downloadPdf(id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `resume-${id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { this.error = 'Failed to download PDF.'; },
    });
  }

  preview(id: number): void {
    window.open(`/api/resumes/${id}/preview`, '_blank');
  }
}
