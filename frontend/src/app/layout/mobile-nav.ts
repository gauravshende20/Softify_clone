import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Icon } from '../shared/components/icon';

@Component({
  selector: 'app-mobile-nav',
  imports: [RouterLink, RouterLinkActive, Icon],
  template: `
    <nav class="nav" aria-label="Mobile">
      <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
        <app-icon name="home" /> Home
      </a>
      <a routerLink="/search" routerLinkActive="active">
        <app-icon name="search" /> Search
      </a>
      <a routerLink="/library" routerLinkActive="active">
        <app-icon name="library" /> Library
      </a>
      <a routerLink="/profile" routerLinkActive="active">
        <app-icon name="user" /> Profile
      </a>
    </nav>
  `,
  styles: `
    :host {
      display: none;
    }
    .nav {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      background: #121218;
      border-top: 1px solid var(--border);
      padding: 0.35rem 0.2rem 0.55rem;
    }
    a {
      display: grid;
      justify-items: center;
      gap: 0.15rem;
      color: var(--muted);
      font-size: 0.7rem;
      font-weight: 650;
    }
    a.active {
      color: var(--accent);
    }
    @media (max-width: 840px) {
      :host {
        display: block;
      }
    }
  `,
})
export class MobileNav {}
