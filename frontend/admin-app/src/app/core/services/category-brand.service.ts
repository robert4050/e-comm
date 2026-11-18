import { Injectable, signal, computed } from '@angular/core';

export interface CategoryItem {
  id: string;
  name: string;
  slug: string;
  itemCount: number;
  status: 'ACTIVE' | 'ARCHIVED';
  description: string;
}

export interface BrandItem {
  id: string;
  name: string;
  origin: string;
  skuCount: number;
  status: 'PARTNER' | 'FEATURED';
}

const STORAGE_KEY_CATEGORIES = 'ecomm_admin_categories';
const STORAGE_KEY_BRANDS = 'ecomm_admin_brands';

const DEFAULT_CATEGORIES: CategoryItem[] = [
  { id: 'cat-1', name: 'Electronics', slug: 'electronics', itemCount: 12, status: 'ACTIVE', description: 'Laptops, desktop workstations, internal SSDs and enterprise monitors' },
  { id: 'cat-2', name: 'Mobile', slug: 'mobile', itemCount: 6, status: 'ACTIVE', description: 'Flagship 5G smartphones, chargers, OLED devices, and mobile tech' },
  { id: 'cat-3', name: 'Audio', slug: 'audio', itemCount: 4, status: 'ACTIVE', description: 'Active noise-canceling headphones, studio monitors, wireless earbuds' },
  { id: 'cat-4', name: 'Wearables', slug: 'wearables', itemCount: 3, status: 'ACTIVE', description: 'Titanium smartwatches, fitness bands, health sensors, GPS trackers' },
  { id: 'cat-5', name: 'Gaming', slug: 'gaming', itemCount: 4, status: 'ACTIVE', description: 'Next-gen gaming consoles, controllers, RGB accessories' },
  { id: 'cat-6', name: 'Cameras', slug: 'cameras', itemCount: 2, status: 'ACTIVE', description: 'Full-frame mirrorless cameras, lenses, video gear' },
  { id: 'cat-7', name: 'Accessories', slug: 'accessories', itemCount: 5, status: 'ACTIVE', description: 'Mechanical keyboards, wireless mice, docking stations, cables' },
  { id: 'cat-8', name: 'Home', slug: 'home', itemCount: 3, status: 'ACTIVE', description: 'Smart vacuums, espresso machines, smart home automation' }
];

const DEFAULT_BRANDS: BrandItem[] = [
  { id: 'b-1', name: 'Apple', origin: 'Cupertino, CA', skuCount: 8, status: 'FEATURED' },
  { id: 'b-2', name: 'Samsung', origin: 'Suwon, South Korea', skuCount: 6, status: 'FEATURED' },
  { id: 'b-3', name: 'Sony', origin: 'Tokyo, Japan', skuCount: 5, status: 'FEATURED' },
  { id: 'b-4', name: 'Dell', origin: 'Round Rock, TX', skuCount: 4, status: 'PARTNER' },
  { id: 'b-5', name: 'Lenovo', origin: 'Beijing, China', skuCount: 3, status: 'PARTNER' },
  { id: 'b-6', name: 'Google', origin: 'Mountain View, CA', skuCount: 3, status: 'PARTNER' },
  { id: 'b-7', name: 'Bose', origin: 'Framingham, MA', skuCount: 2, status: 'PARTNER' },
  { id: 'b-8', name: 'Garmin', origin: 'Olathe, KS', skuCount: 2, status: 'PARTNER' },
  { id: 'b-9', name: 'Keychron', origin: 'Hong Kong', skuCount: 2, status: 'PARTNER' },
  { id: 'b-10', name: 'Logitech', origin: 'Lausanne, Switzerland', skuCount: 3, status: 'PARTNER' },
  { id: 'b-11', name: 'Dyson', origin: 'Malmesbury, UK', skuCount: 2, status: 'PARTNER' },
  { id: 'b-12', name: 'Breville', origin: 'Sydney, Australia', skuCount: 1, status: 'PARTNER' }
];

@Injectable({
  providedIn: 'root'
})
export class CategoryBrandService {
  readonly categories = signal<CategoryItem[]>(this.loadCategories());
  readonly brands = signal<BrandItem[]>(this.loadBrands());

  readonly categoryNames = computed(() => this.categories().map(c => c.name));
  readonly brandNames = computed(() => this.brands().map(b => b.name));

  addCategory(name: string, description: string): void {
    const trimmed = name.trim();
    if (!trimmed) return;
    const newCat: CategoryItem = {
      id: 'cat-' + Date.now().toString().slice(-4),
      name: trimmed,
      slug: trimmed.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
      itemCount: 0,
      status: 'ACTIVE',
      description: description.trim() || 'Store category'
    };
    this.categories.update(list => {
      const updated = [newCat, ...list];
      this.persistCategories(updated);
      return updated;
    });
  }

  deleteCategory(id: string): void {
    this.categories.update(list => {
      const updated = list.filter(c => c.id !== id);
      this.persistCategories(updated);
      return updated;
    });
  }

  addBrand(name: string, origin: string, status: 'PARTNER' | 'FEATURED' = 'PARTNER'): void {
    const trimmed = name.trim();
    if (!trimmed) return;
    const newBrand: BrandItem = {
      id: 'b-' + Date.now().toString().slice(-4),
      name: trimmed,
      origin: origin.trim() || 'Global',
      skuCount: 0,
      status
    };
    this.brands.update(list => {
      const updated = [newBrand, ...list];
      this.persistBrands(updated);
      return updated;
    });
  }

  deleteBrand(id: string): void {
    this.brands.update(list => {
      const updated = list.filter(b => b.id !== id);
      this.persistBrands(updated);
      return updated;
    });
  }

  private loadCategories(): CategoryItem[] {
    try {
      const saved = localStorage.getItem(STORAGE_KEY_CATEGORIES);
      return saved ? JSON.parse(saved) : DEFAULT_CATEGORIES;
    } catch {
      return DEFAULT_CATEGORIES;
    }
  }

  private loadBrands(): BrandItem[] {
    try {
      const saved = localStorage.getItem(STORAGE_KEY_BRANDS);
      return saved ? JSON.parse(saved) : DEFAULT_BRANDS;
    } catch {
      return DEFAULT_BRANDS;
    }
  }

  private persistCategories(items: CategoryItem[]): void {
    try {
      localStorage.setItem(STORAGE_KEY_CATEGORIES, JSON.stringify(items));
    } catch {}
  }

  private persistBrands(items: BrandItem[]): void {
    try {
      localStorage.setItem(STORAGE_KEY_BRANDS, JSON.stringify(items));
    } catch {}
  }
}
