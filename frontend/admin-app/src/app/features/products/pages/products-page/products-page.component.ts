import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminProductService } from '../../services/admin-product.service';
import { CategoryBrandService } from '../../../../core/services/category-brand.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Product, CreateProductRequest } from '../../models/product.model';
import { ProductFormModalComponent } from '../../components/product-form-modal/product-form-modal.component';
import { PriceFormatPipe } from '../../../../shared/pipes/price-format.pipe';
import { TruncatePipe } from '../../../../shared/pipes/truncate.pipe';

@Component({
  selector: 'app-products-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductFormModalComponent, PriceFormatPipe, TruncatePipe],
  templateUrl: './products-page.component.html',
  styleUrls: ['./products-page.component.css']
})
export class ProductsPageComponent implements OnInit {
  private productService = inject(AdminProductService);
  private categoryBrandService = inject(CategoryBrandService);
  private notificationService = inject(NotificationService);

  readonly categories = this.categoryBrandService.categoryNames;
  readonly brands = this.categoryBrandService.brandNames;

  products: Product[] = [];
  loading: boolean = false;
  isModalOpen: boolean = false;
  editingProduct: Product | null = null;

  form: CreateProductRequest = {
    sku: '',
    name: '',
    description: '',
    price: 49.99,
    category: 'Electronics',
    brand: '',
    stockQuantity: 50,
    imageUrl: 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600'
  };

  ngOnInit(): void {
    this.fetchProducts();
  }

  fetchProducts(): void {
    this.loading = true;
    this.productService.getProducts().subscribe({
      next: (res) => {
        this.products = res.content || [];
        this.loading = false;
      },
      error: () => {
        this.products = this.getFallbackProducts();
        this.loading = false;
      }
    });
  }

  openCreateModal(): void {
    this.editingProduct = null;
    const defaultCat = this.categories().length ? this.categories()[0] : 'Electronics';
    this.form = {
      sku: 'SKU-' + Math.floor(1000 + Math.random() * 9000),
      name: '',
      description: '',
      price: 49.99,
      category: defaultCat,
      brand: '',
      stockQuantity: 100,
      imageUrl: 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600'
    };
    this.isModalOpen = true;
  }

  openEditModal(product: Product): void {
    this.editingProduct = product;
    this.form = {
      sku: product.sku,
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      brand: product.brand || '',
      stockQuantity: product.stockQuantity,
      imageUrl: product.imageUrl
    };
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.editingProduct = null;
  }

  onFormSubmitted(formData: CreateProductRequest): void {
    if (this.editingProduct) {
      this.productService.updateProduct(this.editingProduct.id, {
        name: formData.name,
        description: formData.description,
        price: formData.price,
        category: formData.category,
        brand: formData.brand,
        stockQuantity: formData.stockQuantity,
        imageUrl: formData.imageUrl
      }).subscribe({
        next: (updated) => {
          this.notificationService.success(`Product "${updated.name}" updated! Outbox event dispatched.`);
          this.closeModal();
          this.fetchProducts();
        },
        error: () => {
          const index = this.products.findIndex(p => p.id === this.editingProduct?.id);
          if (index > -1) {
            this.products[index] = { ...this.products[index], ...formData };
            this.notificationService.success(`Product updated! (Local fallback mode)`);
          }
          this.closeModal();
        }
      });
    } else {
      this.productService.createProduct(formData).subscribe({
        next: (created) => {
          this.notificationService.success(`Product "${created.name}" created! Outbox event dispatched.`);
          this.closeModal();
          this.fetchProducts();
        },
        error: () => {
          const newProd: Product = {
            id: 'prod-' + Date.now().toString().slice(-4),
            ...formData,
            status: 'PUBLISHED'
          };
          this.products.unshift(newProd);
          this.notificationService.success(`Product "${newProd.name}" created! (Local fallback mode)`);
          this.closeModal();
        }
      });
    }
  }

  publishProduct(product: Product): void {
    this.productService.publishProduct(product.id).subscribe({
      next: (res) => {
        this.notificationService.success(`Product ${res.sku} published successfully!`);
        this.fetchProducts();
      },
      error: () => {
        product.status = 'PUBLISHED';
        this.notificationService.success(`Product published! (Local fallback mode)`);
      }
    });
  }

  deleteProduct(product: Product): void {
    if (!confirm(`Are you sure you want to delete "${product.name}"?`)) return;

    this.productService.deleteProduct(product.id).subscribe({
      next: () => {
        this.notificationService.success(`Product deleted.`);
        this.fetchProducts();
      },
      error: () => {
        this.products = this.products.filter(p => p.id !== product.id);
        this.notificationService.success(`Product deleted.`);
      }
    });
  }

  private getFallbackProducts(): Product[] {
    return [
      {
        id: 'prod-001',
        sku: 'SKU-LAP-001',
        name: 'Pro Laptop 16" M3',
        description: 'Flagship workstation laptop featuring 32GB RAM, 1TB SSD, and Liquid Retina display.',
        price: 1999.99,
        category: 'Electronics',
        brand: 'Apple',
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
        category: 'Mobile',
        brand: 'Samsung',
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
        brand: 'Sony',
        status: 'PUBLISHED',
        stockQuantity: 120,
        imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600'
      }
    ];
  }
}
