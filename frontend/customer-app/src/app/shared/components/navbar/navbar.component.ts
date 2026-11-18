import { Component, Output, EventEmitter, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CartService } from '../../../features/cart/services/cart.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthModalComponent } from '../auth-modal/auth-modal.component';
import { CatalogService } from '../../../features/catalog/services/catalog.service';
import { Suggestion } from '../../../features/catalog/models/product.model';
import { Subject, Subscription, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, AuthModalComponent],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  cartService = inject(CartService);
  private notificationService = inject(NotificationService);
  private catalogService = inject(CatalogService);
  private router = inject(Router);

  @Output() searchChanged = new EventEmitter<string>();
  @Output() cartToggled = new EventEmitter<void>();

  searchQuery: string = '';
  suggestions: Suggestion[] = [];
  isSearching: boolean = false;
  showDropdown: boolean = false;

  private searchSubject = new Subject<string>();
  private searchSub: Subscription | null = null;

  ngOnInit(): void {
    this.searchSub = this.searchSubject.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(term => {
        if (!term || term.trim().length < 2) {
          this.suggestions = [];
          this.isSearching = false;
          this.showDropdown = false;
          return of([]);
        }
        this.isSearching = true;
        return this.catalogService.getSuggestions(term).pipe(
          catchError(() => of([]))
        );
      })
    ).subscribe(results => {
      this.suggestions = results || [];
      this.isSearching = false;
      this.showDropdown = this.suggestions.length > 0;
    });
  }

  ngOnDestroy(): void {
    if (this.searchSub) {
      this.searchSub.unsubscribe();
    }
  }

  onInput(value: string): void {
    this.searchSubject.next(value);
  }

  onSelectSuggestion(suggestion: Suggestion): void {
    this.searchQuery = suggestion.name;
    this.showDropdown = false;
    this.router.navigate(['/catalog'], { queryParams: { search: suggestion.name } });
    this.searchChanged.emit(suggestion.name);
  }

  onBlur(): void {
    // Small delay to allow click on suggestion item to register
    setTimeout(() => {
      this.showDropdown = false;
    }, 200);
  }

  onFocus(): void {
    if (this.suggestions.length > 0) {
      this.showDropdown = true;
    }
  }

  onSearch(): void {
    this.showDropdown = false;
    this.router.navigate(['/catalog'], { queryParams: { search: this.searchQuery.trim() } });
    this.searchChanged.emit(this.searchQuery);
  }

  toggleCart(): void {
    if (!this.authService.currentUser()) {
      this.notificationService.info('Please sign in to view your cart.');
      this.authService.openAuthModal('signin');
      return;
    }
    this.cartToggled.emit();
  }

  openSignIn(): void {
    this.authService.openAuthModal('signin');
  }

  openSignUp(): void {
    this.authService.openAuthModal('signup');
  }
}
