import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <nav>
      <a class="nav-brand" routerLink="/resumes">CV Generator</a>
      <a routerLink="/resumes">Résumés</a>
      <a routerLink="/resumes/new">+ New</a>
    </nav>
    <router-outlet />
  `,
})
export class AppComponent {}
