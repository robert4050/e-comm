import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  authService = inject(AuthService);
  notificationService = inject(NotificationService);

  openSignIn(): void {
    this.authService.openAuthModal('signin');
  }

  openSignUp(): void {
    this.authService.openAuthModal('signup');
  }

  logout(): void {
    this.authService.logout();
    this.notificationService.info('Signed out of admin console');
  }
}
