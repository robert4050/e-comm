import { Injectable, signal } from '@angular/core';

export interface NotificationMessage {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private messagesSignal = signal<NotificationMessage[]>([]);
  readonly messages = this.messagesSignal.asReadonly();

  show(text: string, type: 'success' | 'error' | 'info' | 'warning' = 'info', durationMs: number = 4000): void {
    const id = Math.random().toString(36).substring(2, 9);
    const msg: NotificationMessage = { id, type, text };
    this.messagesSignal.update(list => [...list, msg]);

    setTimeout(() => {
      this.dismiss(id);
    }, durationMs);
  }

  success(text: string): void {
    this.show(text, 'success');
  }

  info(text: string): void {
    this.show(text, 'info');
  }

  error(text: string): void {
    this.show(text, 'error', 6000);
  }

  warning(text: string): void {
    this.show(text, 'warning', 5000);
  }

  dismiss(id: string): void {
    this.messagesSignal.update(list => list.filter(m => m.id !== id));
  }
}
