import { Component, input } from '@angular/core';
import { Icon } from './icon';

@Component({
  selector: 'app-empty-state',
  imports: [Icon],
  template: `
    <div class="empty">
      <app-icon [name]="icon()" />
      <h2>{{ title() }}</h2>
      <p>{{ message() }}</p>
      <ng-content />
    </div>
  `,
  styles: `
    .empty {
      display: grid;
      justify-items: center;
      gap: 0.6rem;
      padding: 3rem 1rem;
      text-align: center;
      color: var(--muted);
    }
    app-icon {
      width: 2.2rem;
      height: 2.2rem;
      color: var(--accent);
    }
    h2 {
      margin: 0;
      color: var(--text);
      font-size: 1.15rem;
    }
    p {
      margin: 0;
      max-width: 28rem;
    }
  `,
})
export class EmptyState {
  readonly title = input('Nothing here yet');
  readonly message = input('Play a song or follow an artist to fill this space.');
  readonly icon = input('music');
}
