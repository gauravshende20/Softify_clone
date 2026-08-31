import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-shell',
  imports: [RouterOutlet],
  template: `
    <div class="wrap">
      <header>
        <img src="/logo.svg" width="40" height="40" alt="" />
        <div>
          <p class="eyebrow">Warm sound, late nights</p>
          <h1>Harmonia</h1>
        </div>
      </header>
      <router-outlet />
    </div>
  `,
  styles: `
    :host {
      display: grid;
      min-height: 100dvh;
      place-items: center;
      padding: 1.5rem;
      background:
        radial-gradient(circle at top left, rgba(232, 184, 109, 0.16), transparent 36%),
        var(--bg);
    }
    .wrap {
      width: min(28rem, 100%);
      display: grid;
      gap: 1.5rem;
    }
    header {
      display: flex;
      align-items: center;
      gap: 0.85rem;
    }
    h1 {
      margin: 0;
      font-family: var(--font-display);
      font-size: 2.2rem;
      letter-spacing: -0.04em;
    }
    .eyebrow {
      margin: 0;
      color: var(--accent);
      font-size: 0.78rem;
      letter-spacing: 0.12em;
      text-transform: uppercase;
    }
  `,
})
export class AuthShell {}
