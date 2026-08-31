import { Pipe } from '@angular/core';

@Pipe({ name: 'duration' })
export class DurationPipe {
  transform(seconds: number | null | undefined): string {
    if (seconds == null || Number.isNaN(seconds) || seconds < 0) {
      return '0:00';
    }
    const total = Math.floor(seconds);
    const hours = Math.floor(total / 3600);
    const minutes = Math.floor((total % 3600) / 60);
    const secs = total % 60;
    const pad = (n: number) => n.toString().padStart(2, '0');
    return hours > 0 ? `${hours}:${pad(minutes)}:${pad(secs)}` : `${minutes}:${pad(secs)}`;
  }
}
