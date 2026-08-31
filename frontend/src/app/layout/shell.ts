import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PlayerBar } from '../player/player-bar';
import { QueuePanel } from '../player/queue-panel';
import { ToastHost } from '../shared/components/toast-host';
import { MobileNav } from './mobile-nav';
import { Sidebar } from './sidebar';
import { TopBar } from './top-bar';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, Sidebar, TopBar, PlayerBar, MobileNav, QueuePanel, ToastHost],
  template: `
    <div class="shell">
      <a class="skip-link" href="#main">Skip to content</a>
      <app-sidebar />
      <div class="column">
        <app-top-bar />
        <main id="main" class="content">
          <router-outlet />
        </main>
      </div>
      <app-player-bar />
      <app-mobile-nav />
      <app-queue-panel />
      <app-toast-host />
    </div>
  `,
  styles: `
    .shell {
      min-height: 100dvh;
      display: grid;
      grid-template-columns: 16.5rem minmax(0, 1fr);
      grid-template-rows: 1fr auto auto;
      grid-template-areas:
        'sidebar column'
        'player player'
        'mobile mobile';
    }
    app-sidebar {
      grid-area: sidebar;
    }
    .column {
      grid-area: column;
      min-width: 0;
      display: grid;
      grid-template-rows: auto 1fr;
      min-height: 0;
    }
    .content {
      min-height: 0;
      overflow: auto;
      padding: 0.5rem 1.4rem 1.5rem;
    }
    app-player-bar {
      grid-area: player;
    }
    app-mobile-nav {
      grid-area: mobile;
    }
    @media (max-width: 840px) {
      .shell {
        grid-template-columns: 1fr;
        grid-template-areas:
          'column'
          'player'
          'mobile';
      }
      .content {
        padding: 0.4rem 0.85rem 1rem;
      }
    }
  `,
})
export class Shell {}
