import { Component, computed, input } from '@angular/core';

const PATHS: Record<string, string> = {
  home: 'M4 11.5 12 4l8 7.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z',
  search:
    'M10.5 4a6.5 6.5 0 1 1 0 13 6.5 6.5 0 0 1 0-13m0-2a8.5 8.5 0 1 0 5.3 15.1l4.05 4.06 1.42-1.42-4.06-4.05A8.5 8.5 0 0 0 10.5 2',
  library: 'M4 4h2v16H4zm4 0h2v16H8zm5.2 1.2 8.8-2.2v16.1l-8.8 2.2z',
  heart:
    'M12 21s-7.2-4.6-9.4-8.3C.7 9.7 2.2 6 5.6 6c1.9 0 3.4 1.1 4.4 2.6C11 7.1 12.5 6 14.4 6c3.4 0 4.9 3.7 3 6.7C19.2 16.4 12 21 12 21z',
  play: 'M8 5.2v13.6L19 12z',
  pause: 'M6 5h4v14H6zm8 0h4v14h-4z',
  next: 'M6 6l8 6-8 6zm10 0h2v12h-2z',
  prev: 'M18 6l-8 6 8 6zM6 6h2v12H6z',
  shuffle:
    'M16 4h4v4h-2V6.4l-4.2 4.2-1.4-1.4L16.6 5H16zm-9.2.8L12 10l-1.4 1.4-5.2-5.2zM16 20h4v-4h-2v1.6l-4.2-4.2-1.4 1.4 4.2 4.2H16zM6.8 19.2 12 14l-1.4-1.4-5.2 5.2z',
  repeat:
    'M7 7h8.5a3.5 3.5 0 0 1 0 7H15v-2h.5a1.5 1.5 0 0 0 0-3H7v2.5L3 8l4-3.5zm10 10H8.5a3.5 3.5 0 0 1 0-7H9v2h-.5a1.5 1.5 0 0 0 0 3H17v-2.5L21 16l-4 3.5z',
  volume:
    'M4 9h3.2L11 6v12l-3.8-3H4zm11.2-3.2 1.4 1.4A5 5 0 0 1 18 12a5 5 0 0 1-1.4 4.8l-1.4-1.4A3 3 0 0 0 16 12a3 3 0 0 0-.8-2.2z',
  mute: 'M4 9h3.2L11 6v12l-3.8-3H4zm12.6-2.6 1.4 1.4L16.4 12l1.6 4.2-1.4 1.4L15 13.4l-1.6 4.2-1.4-1.4L13.6 12l-1.6-4.2 1.4-1.4L15 10.6z',
  queue: 'M4 6h16v2H4zm0 5h16v2H4zm0 5h10v2H4z',
  user: 'M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4m0 2c-4 0-8 2-8 5v1h16v-1c0-3-4-5-8-5z',
  logout: 'M10 5H5v14h5v2H3V3h7zm9 7-4-4v3H9v2h6v3z',
  plus: 'M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z',
  close: 'M6.2 5 5 6.2 10.8 12 5 17.8 6.2 19 12 13.2 17.8 19 19 17.8 13.2 12 19 6.2 17.8 5 12 10.8z',
  follow: 'M15 8a4 4 0 1 1-8 0 4 4 0 0 1 8 0m-4 6c-4 0-8 2-8 5v1h9.4a6.5 6.5 0 0 1-.4-2 6.5 6.5 0 0 1 6.5-6.4C16.4 14.6 14.4 14 11 14m7.5 3v-2h2v2h2v2h-2v2h-2v-2h-2v-2z',
  upload: 'M11 15V7.8L8.4 10.4 7 9l5-5 5 5-1.4 1.4L13 7.8V15zM5 18h14v2H5z',
  admin: 'M12 2 4 6v6c0 5 3.4 9.4 8 10 4.6-.6 8-5 8-10V6zm0 6a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5m-4 9.2c.7-1.6 2.2-2.2 4-2.2s3.3.6 4 2.2C14.7 18.5 13.4 19 12 19s-2.7-.5-4-1.8z',
  studio: 'M4 4h6v16H4zm8 4h3v12h-3zm5-2h3v14h-3z',
  bell: 'M12 3a6 6 0 0 1 6 6v3.2l1.4 2.8H4.6L6 12.2V9a6 6 0 0 1 6-6m0 18a2.5 2.5 0 0 1-2.5-2.5h5A2.5 2.5 0 0 1 12 21z',
  chevron: 'M9 6l6 6-6 6-1.4-1.4L12.2 12 7.6 7.4z',
  more: 'M6 10.5A1.5 1.5 0 1 1 6 13.5 1.5 1.5 0 0 1 6 10.5m6 0A1.5 1.5 0 1 1 12 13.5 1.5 1.5 0 0 1 12 10.5m6 0A1.5 1.5 0 1 1 18 13.5 1.5 1.5 0 0 1 18 10.5',
  music:
    'M9 4v11.2A3.4 3.4 0 1 0 11 18V8h8V4zm0 14.2A1.4 1.4 0 1 1 7.6 17 1.4 1.4 0 0 1 9 18.2z',
};

@Component({
  selector: 'app-icon',
  template: `
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path [attr.d]="d()" />
    </svg>
  `,
  styles: `
    :host {
      display: inline-flex;
      width: 1.25rem;
      height: 1.25rem;
      flex: 0 0 auto;
    }
    svg {
      width: 100%;
      height: 100%;
      fill: currentColor;
    }
  `,
})
export class Icon {
  readonly name = input.required<string>();
  readonly d = computed(() => PATHS[this.name()] ?? PATHS['music']);
}
