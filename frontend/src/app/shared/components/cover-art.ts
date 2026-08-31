import { Component, computed, input } from '@angular/core';
import { coverGradient, initials } from '../utils/format';

@Component({
  selector: 'app-cover-art',
  template: `
    @if (src()) {
      <img [src]="src()!" [alt]="alt()" loading="lazy" />
    } @else {
      <span class="fallback" [style.background]="gradient()">{{ label() }}</span>
    }
  `,
  styles: `
    :host {
      display: block;
      overflow: hidden;
      background: #1a1a22;
      aspect-ratio: 1;
    }
    img,
    .fallback {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .fallback {
      display: grid;
      place-items: center;
      color: #e8b86d;
      font-weight: 650;
      letter-spacing: 0.04em;
    }
  `,
})
export class CoverArt {
  readonly src = input<string | undefined | null>();
  readonly alt = input('Cover');
  readonly seed = input<string | undefined>('');
  readonly gradient = computed(() => coverGradient(this.seed() || this.alt()));
  readonly label = computed(() => initials(this.seed() || this.alt()));
}
