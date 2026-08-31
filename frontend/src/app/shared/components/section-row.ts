import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-section-row',
  imports: [RouterLink],
  template: `
    <section class="section">
      <header>
        <h2>{{ title() }}</h2>
        @if (link()) {
          <a [routerLink]="link()">{{ linkLabel() }}</a>
        }
      </header>
      <div class="rail">
        <ng-content />
      </div>
    </section>
  `,
  styles: `
    .section {
      display: grid;
      gap: 0.75rem;
    }
    header {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      gap: 1rem;
    }
    h2 {
      margin: 0;
      font-size: 1.35rem;
    }
    a {
      color: var(--muted);
      font-size: 0.85rem;
      font-weight: 650;
    }
    .rail {
      display: grid;
      grid-auto-flow: column;
      grid-auto-columns: minmax(10.5rem, 14rem);
      gap: 0.4rem;
      overflow-x: auto;
      padding-bottom: 0.4rem;
      scroll-snap-type: x mandatory;
    }
    .rail > * {
      scroll-snap-align: start;
    }
  `,
})
export class SectionRow {
  readonly title = input.required<string>();
  readonly link = input<string | unknown[] | null>(null);
  readonly linkLabel = input('Show all');
}
