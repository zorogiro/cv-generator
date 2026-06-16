import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <h1>Login</h1>
      </div>

      <div *ngIf="serverError" class="alert alert-error">
        {{ serverError }}
        <button class="alert-close" (click)="serverError = ''">×</button>
      </div>

      <form [formGroup]="form" (ngSubmit)="submit()" novalidate class="form-section">
        <div class="form-group">
          <label>Email *</label>
          <input type="email" formControlName="email" autocomplete="email">
          <span class="field-error" *ngIf="form.get('email')?.touched && form.get('email')?.errors?.['required']">Required</span>
          <span class="field-error" *ngIf="form.get('email')?.touched && form.get('email')?.errors?.['email']">Must be a valid email</span>
        </div>

        <div class="form-group">
          <label>Password *</label>
          <input type="password" formControlName="password" autocomplete="current-password">
          <span class="field-error" *ngIf="form.get('password')?.touched && form.get('password')?.errors?.['required']">Required</span>
        </div>

        <div class="form-footer">
          <a routerLink="/register" class="btn">Register instead</a>
          <button type="submit" class="btn btn-primary" [disabled]="submitting">
            {{ submitting ? 'Logging in…' : 'Login' }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  submitting = false;
  serverError = '';

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.serverError = '';

    const { email, password } = this.form.getRawValue();
    this.auth.login({ email: email!, password: password! }).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/resumes']);
      },
      error: (err) => {
        this.submitting = false;
        this.serverError = err.status === 401
          ? 'Invalid email or password'
          : err.status === 409
            ? 'Email already exists'
            : 'Login failed';
      }
    });
  }
}