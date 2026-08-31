import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DurationPipe } from '../shared/pipes/duration-pipe';
import { CoverArt } from '../shared/components/cover-art';
import { Icon } from '../shared/components/icon';
import { PlayerStore } from './player-store';

@Component({
  selector: 'app-player-bar',
  imports: [RouterLink, DurationPipe, CoverArt, Icon],
  templateUrl: './player-bar.html',
  styleUrl: './player-bar.scss',
})
export class PlayerBar {
  readonly player = inject(PlayerStore);

  onSeek(event: MouseEvent): void {
    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const ratio = (event.clientX - rect.left) / rect.width;
    this.player.seekRatio(ratio);
  }

  onSeekKey(event: KeyboardEvent): void {
    if (event.key === 'ArrowRight') {
      this.player.seek(this.player.position() + 5);
    } else if (event.key === 'ArrowLeft') {
      this.player.seek(this.player.position() - 5);
    }
  }

  onVolume(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value);
    this.player.setVolume(value);
  }
}
