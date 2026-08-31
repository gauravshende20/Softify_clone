import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { httpMessage } from '../../../shared/utils/format';

const PASSWORD = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{10,72}$/;

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../login/login.scss',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly submitting = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    displayName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD)]],
    role: this.fb.nonNullable.control<'LISTENER' | 'ARTIST'>('LISTENER', Validators.required),
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set('');
    const { displayName, email, password, role } = this.form.getRawValue();
    this.auth.register({ email, password, role, displayName }).subscribe({
      next: () => void this.router.navigateByUrl('/'),
      error: (err) => {
        this.submitting.set(false);
        this.error.set(httpMessage(err, 'Could not create the account.'));
      },
    });
  }
}
