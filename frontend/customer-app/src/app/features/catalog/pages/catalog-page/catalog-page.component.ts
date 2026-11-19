import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CatalogService } from '../../services/catalog.service';
import { CartService } from '../../../cart/services/cart.service';
import { AuthService } from '../../../../core/auth/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Product, SearchFacets } from '../../models/product.model';
import { ProductCardComponent } from '../../components/product-card/product-card.component';

export interface HeroSlide {
  id: number;
  image: string;
  badge?: string;
  tag?: string;
  title: string;
  subtitle: string;
  ctaText: string;
  category: string;
}

@Component({
  selector: 'app-catalog-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductCardComponent],
  templateUrl: './catalog-page.component.html',
  styleUrls: ['./catalog-page.component.css']
})
export class CatalogPageComponent implements OnInit, OnDestroy {
  private catalogService = inject(CatalogService);
  private cartService = inject(CartService);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  products: Product[] = [];
  categories: string[] = ['All', 'Electronics', 'Audio', 'Wearables', 'Accessories', 'Mobile', 'Laptops', 'Cameras', 'Gaming', 'Home'];
  selectedCategory: string = 'All';
  selectedBrand: string = 'All';
  searchQuery: string = '';
  sortBy: string = 'default';
  inStockOnly: boolean = false;
  minPrice: number | null = null;
  maxPrice: number | null = null;
  loading: boolean = false;

  // Elasticsearch Facets
  facets: SearchFacets | null = null;
  brandList: { name: string; count: number }[] = [];
  totalCatalogHits: number = 0;

  private queryParamSub: Subscription | null = null;

  // Hero Carousel State
  currentSlide: number = 0;
  private autoplayTimer: any = null;
  private isPaused: boolean = false;

  heroSlides: HeroSlide[] = [
    {
      id: 1,
      image: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=1600&auto=format&fit=crop&q=80',
      title: 'Next-Gen Performance & Pro Workstations',
      subtitle: 'Experience uncompromised power with Apple M3 Max, Dell XPS InfinityEdge, and cutting-edge silicon architectures.',
      ctaText: 'Explore Laptops',
      category: 'Electronics'
    },
    {
      id: 2,
      image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=1600&auto=format&fit=crop&q=80',
      badge: 'Free Express Delivery',
      title: 'Galaxy S24 Ultra & Flagship Smartphones',
      subtitle: 'Crystal-clear AMOLED 120Hz displays, AI-driven computational photography, and ultra-fast 5G connectivity.',
      ctaText: 'Shop Smartphones',
      category: 'Electronics'
    },
    {
      id: 3,
      image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1600&auto=format&fit=crop&q=80',
      tag: 'IMMERSIVE SOUND',
      title: 'Acoustic Precision & Active Noise Cancellation',
      subtitle: 'Precision studio-tuned drivers with 40-hour wireless battery endurance, transparency mode, and spatial audio.',
      ctaText: 'Discover Audio',
      category: 'Audio'
    },
    {
      id: 4,
      image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1600&auto=format&fit=crop&q=80',
      badge: 'Trending Collection',
      tag: 'SMART WEARABLES',
      title: 'Intelligent Fitness & Wellness on Your Wrist',
      subtitle: 'Rugged titanium cases, GPS tracking, heart-rate zones, and round-the-clock health diagnostics.',
      ctaText: 'Shop Wearables',
      category: 'Wearables'
    }
  ];

  ngOnInit(): void {
    this.startAutoplay();
    this.fetchFacets();

    // Listen to query parameters from URL / Navbar search
    this.queryParamSub = this.route.queryParams.subscribe(params => {
      this.searchQuery = params['search'] !== undefined ? params['search'] : '';
      this.selectedCategory = params['category'] !== undefined ? params['category'] : 'All';
      this.selectedBrand = params['brand'] !== undefined ? params['brand'] : 'All';
      this.fetchProducts();
      this.fetchFacets();
    });
  }

  ngOnDestroy(): void {
    this.stopAutoplay();
    if (this.queryParamSub) {
      this.queryParamSub.unsubscribe();
    }
  }

  startAutoplay(): void {
    this.stopAutoplay();
    this.autoplayTimer = setInterval(() => {
      if (!this.isPaused) {
        this.nextSlide();
      }
    }, 4500);
  }

  stopAutoplay(): void {
    if (this.autoplayTimer) {
      clearInterval(this.autoplayTimer);
      this.autoplayTimer = null;
    }
  }

  pauseAutoplay(): void {
    this.isPaused = true;
  }

  resumeAutoplay(): void {
    this.isPaused = false;
  }

  nextSlide(): void {
    this.currentSlide = (this.currentSlide + 1) % this.heroSlides.length;
  }

  prevSlide(): void {
    this.currentSlide = (this.currentSlide - 1 + this.heroSlides.length) % this.heroSlides.length;
  }

  goToSlide(index: number): void {
    this.currentSlide = index;
    this.startAutoplay(); // Reset interval countdown on manual interaction
  }

  onHeroCta(category: string): void {
    this.onCategorySelect(category);
    const catalogEl = document.getElementById('catalog-section');
    if (catalogEl) {
      catalogEl.scrollIntoView({ behavior: 'smooth' });
    }
  }

  fetchProducts(): void {
    this.loading = true;
    this.catalogService.getProducts(
      this.selectedCategory,
      this.searchQuery,
      0,
      12,
      this.selectedBrand,
      this.minPrice ?? undefined,
      this.maxPrice ?? undefined,
      this.inStockOnly || undefined,
      this.sortBy
    ).subscribe({
      next: (response) => {
        this.products = response.content || [];
        this.totalCatalogHits = response.totalElements;
        this.loading = false;
      },
      error: () => {
        this.products = this.getFallbackCatalog();
        this.loading = false;
      }
    });
  }

  fetchFacets(): void {
    this.catalogService.getFacets(this.selectedCategory, this.searchQuery).subscribe({
      next: (res) => {
        this.facets = res;
        if (res && res.brands) {
          this.brandList = Object.entries(res.brands).map(([name, count]) => ({ name, count }));
        }
      },
      error: (err) => {
        console.warn('Failed to load dynamic facets from Elasticsearch:', err);
      }
    });
  }

  onCategorySelect(category: string): void {
    this.selectedCategory = category;
    this.selectedBrand = 'All'; // Reset brand on category switch
    this.fetchProducts();
    this.fetchFacets();
  }

  onBrandSelect(brand: string): void {
    this.selectedBrand = brand;
    this.fetchProducts();
  }

  onSortChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.sortBy = select.value;
    this.fetchProducts();
  }

  onInStockToggle(): void {
    this.inStockOnly = !this.inStockOnly;
    this.fetchProducts();
  }

  clearFilters(): void {
    this.selectedCategory = 'All';
    this.selectedBrand = 'All';
    this.searchQuery = '';
    this.sortBy = 'default';
    this.inStockOnly = false;
    this.minPrice = null;
    this.maxPrice = null;
    this.router.navigate(['/catalog']);
    this.fetchProducts();
    this.fetchFacets();
  }

  onAddToCart(product: Product): void {
    if (!this.authService.currentUser()) {
      this.notificationService.warning('Please sign in to add products to your cart and start shopping.');
      this.authService.openAuthModal('signin');
      return;
    }
    this.cartService.addToCart(product);
    this.notificationService.success(`${product.name} added to cart.`);
  }

  private getFallbackCatalog(): Product[] {
    return [
      {
        id: 'prod-001',
        sku: 'SKU-LAP-001',
        name: 'Pro Laptop 16" M3',
        description: 'Flagship workstation laptop featuring 32GB RAM, 1TB SSD, and Liquid Retina display.',
        price: 1999.99,
        category: 'Electronics',
        status: 'PUBLISHED',
        stockQuantity: 45,
        imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600'
      },
      {
        id: 'prod-002',
        sku: 'SKU-PHN-002',
        name: 'Flagship Smartphone Ultra',
        description: 'Next-gen smartphone with AMOLED 120Hz display, 200MP camera system and 5000mAh battery.',
        price: 1099.00,
        category: 'Electronics',
        status: 'PUBLISHED',
        stockQuantity: 80,
        imageUrl: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600'
      },
      {
        id: 'prod-003',
        sku: 'SKU-AUD-003',
        name: 'Wireless Noise-Canceling Headphones',
        description: 'Premium over-ear headphones with active noise cancellation and 40h battery playback.',
        price: 349.50,
        category: 'Audio',
        status: 'PUBLISHED',
        stockQuantity: 120,
        imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600'
      },
      {
        id: 'prod-004',
        sku: 'SKU-WCH-004',
        name: 'Smart Fitness Watch Series 9',
        description: 'Rugged outdoor smartwatch with heart-rate monitoring, GPS tracking and waterproof design.',
        price: 299.00,
        category: 'Wearables',
        status: 'PUBLISHED',
        stockQuantity: 60,
        imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'
      },
      {
        id: 'prod-005',
        sku: 'SKU-DSK-005',
        name: 'Ergonomic Mechanical Keyboard',
        description: 'Custom mechanical keyboard with hot-swappable switches, RGB backlight and aluminum frame.',
        price: 149.99,
        category: 'Accessories',
        status: 'PUBLISHED',
        stockQuantity: 200,
        imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600'
      }
    ];
  }
}
