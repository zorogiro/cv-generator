import { Routes } from '@angular/router';
import { ResumeListComponent } from './resume-list/resume-list.component';
import { ResumeFormComponent } from './resume-form/resume-form.component';

export const routes: Routes = [
  { path: '', redirectTo: 'resumes', pathMatch: 'full' },
  { path: 'resumes', component: ResumeListComponent },
  { path: 'resumes/new', component: ResumeFormComponent },
  { path: 'resumes/:id/edit', component: ResumeFormComponent },
];
