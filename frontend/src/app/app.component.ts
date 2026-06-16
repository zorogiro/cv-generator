import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  template: `
    <nav>
      <a class="nav-brand" routerLink="/resumes">CV Generator</a>
      <a *ngIf="auth.authenticated()" routerLink="/resumes">Résumés</a>
      <a *ngIf="auth.authenticated()" routerLink="/resumes/new">+ New</a>
      <a *ngIf="!auth.authenticated()" routerLink="/login">Login</a>
      <button *ngIf="auth.authenticated()" class="btn btn-sm" (click)="auth.logout()">Logout</button>
    </nav>
    <router-outlet />
  `,
})
export class AppComponent {
  auth = inject(AuthService);
}