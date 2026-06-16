import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

export interface AuthResponse {
  token: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly TOKEN_KEY = 'auth_token';
  private readonly _authenticated = signal(!!localStorage.getItem(this.TOKEN_KEY));

  readonly authenticated = this._authenticated.asReadonly();

  register(data: RegisterRequest) {
    return this.http.post<AuthResponse>('/api/auth/register', data).pipe(
      tap(res => this.setToken(res.token))
    );
  }

  login(data: LoginRequest) {
    return this.http.post<AuthResponse>('/api/auth/login', data).pipe(
      tap(res => this.setToken(res.token))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this._authenticated.set(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this._authenticated.set(true);
  }
}