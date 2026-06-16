import { Routes } from '@angular/router';
import { ResumeListComponent } from './resume-list/resume-list.component';
import { ResumeFormComponent } from './resume-form/resume-form.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'resumes', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'resumes', component: ResumeListComponent, canActivate: [authGuard] },
  { path: 'resumes/new', component: ResumeFormComponent, canActivate: [authGuard] },
  { path: 'resumes/:id/edit', component: ResumeFormComponent, canActivate: [authGuard] },
];