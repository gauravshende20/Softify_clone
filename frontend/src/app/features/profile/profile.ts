import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { rxResource } from '@angular/core/rxjs-interop';
import { tap } from 'rxjs';
import { AuthService } from '../../core/auth/auth';
import { UserService } from '../../core/services/user';
import { ToastService } from '../../core/services/toast';
import { httpMessage } from '../../shared/utils/format';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  private readonly fb = inject(FormBuilder);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);
  readonly saving = signal(false);
  readonly form = this.fb.nonNullable.group({
    displayName: ['', [Validators.required, Validators.minLength(2)]],
    bio: [''],
    country: [''],
    avatarUrl: [''],
  });

  readonly profile = rxResource({
    stream: () =>
      this.users.me().pipe(
        tap((profile) => {
          if (profile) {
            this.form.patchValue({
              displayName: profile.displayName || this.auth.displayName(),
              bio: profile.bio || '',
              country: profile.country || '',
              avatarUrl: profile.avatarUrl || '',
            });
          } else {
            this.form.patchValue({ displayName: this.auth.displayName() });
          }
        }),
      ),
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.users.updateMe(this.form.getRawValue()).subscribe({
      next: (profile) => {
        this.saving.set(false);
        this.auth.patchCurrentUser({
          displayName: profile.displayName,
          avatarUrl: profile.avatarUrl,
        });
        this.toast.success('Profile updated');
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.error(httpMessage(err, 'Could not save profile'));
      },
    });
  }
}
