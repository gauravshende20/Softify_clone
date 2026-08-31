import { Component, DestroyRef, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../core/auth/auth';
import { NotificationService } from '../core/services/notification';
import { SearchService } from '../core/services/search';
import { AutocompleteItem } from '../core/models/search';
import { Icon } from '../shared/components/icon';
import { initials } from '../shared/utils/format';

@Component({
  selector: 'app-top-bar',
  imports: [ReactiveFormsModule, RouterLink, Icon],
  templateUrl: './top-bar.html',
  styleUrl: './top-bar.scss',
})
export class TopBar {
  private readonly router = inject(Router);
  private readonly searchApi = inject(SearchService);
  readonly notifications = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  readonly auth = inject(AuthService);
  readonly query = new FormControl('', { nonNullable: true });
  readonly suggestions = signal<AutocompleteItem[]>([]);
  readonly open = signal(false);
  readonly notesOpen = signal(false);
  readonly menuOpen = signal(false);
  readonly searchBox = viewChild<ElementRef<HTMLInputElement>>('searchBox');
  readonly unread = this.notifications.unreadCount;
  readonly notes = this.notifications.items;
  readonly initials = initials;

  constructor() {
    this.notifications.refresh().subscribe();
    this.query.valueChanges
      .pipe(
        debounceTime(220),
        distinctUntilChanged(),
        switchMap((value) => (value.trim().length < 2 ? of([]) : this.searchApi.autocomplete(value.trim()))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((items) => {
        this.suggestions.set(items);
        this.open.set(items.length > 0);
      });
  }

  submit(): void {
    const q = this.query.value.trim();
    if (!q) {
      return;
    }
    this.open.set(false);
    void this.router.navigate(['/search'], { queryParams: { q } });
  }

  goTo(item: AutocompleteItem): void {
    this.open.set(false);
    const routes: Record<string, string> = {
      track: '/tracks',
      artist: '/artists',
      album: '/albums',
      playlist: '/playlists',
      genre: '/browse/genre',
    };
    void this.router.navigate([routes[item.type] ?? '/search', item.id]);
  }

  logout(): void {
    this.auth.logout();
  }
}
