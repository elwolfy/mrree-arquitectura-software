import { Injectable, signal } from '@angular/core';
import { ProblemDetail } from '../models/problem-detail.model';

@Injectable({ providedIn: 'root' })
export class ErrorStateService {
  private readonly ultimoErrorSignal = signal<ProblemDetail | null>(null);

  readonly ultimoError = this.ultimoErrorSignal.asReadonly();

  registrarError(problema: ProblemDetail): void {
    this.ultimoErrorSignal.set(problema);
  }

  limpiarError(): void {
    this.ultimoErrorSignal.set(null);
  }
}

