import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryBrandService, CategoryItem, BrandItem } from '../../../../core/services/category-brand.service';
import { NotificationService } from '../../../../core/services/notification.service';

export type { CategoryItem, BrandItem };

@Component({
  selector: 'app-categories-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories-page.component.html',
  styleUrls: ['./categories-page.component.css']
})
export class CategoriesPageComponent {
  private categoryBrandService = inject(CategoryBrandService);
  private notificationService = inject(NotificationService);

  readonly categories = this.categoryBrandService.categories;
  readonly brands = this.categoryBrandService.brands;

  // Category Modal State
  isCatModalOpen = false;
  newCatName = '';
  newCatDesc = '';

  // Brand Modal State
  isBrandModalOpen = false;
  newBrandName = '';
  newBrandOrigin = '';
  newBrandStatus: 'PARTNER' | 'FEATURED' = 'PARTNER';

  openCatModal(): void {
    this.newCatName = '';
    this.newCatDesc = '';
    this.isCatModalOpen = true;
  }

  closeCatModal(): void {
    this.isCatModalOpen = false;
    this.newCatName = '';
    this.newCatDesc = '';
  }

  saveCategory(): void {
    if (!this.newCatName.trim()) {
      this.notificationService.warning('Category name cannot be empty');
      return;
    }
    this.categoryBrandService.addCategory(this.newCatName, this.newCatDesc);
    this.notificationService.success(`Category "${this.newCatName.trim()}" added! Available in product creation.`);
    this.closeCatModal();
  }

  deleteCategory(cat: CategoryItem): void {
    if (!confirm(`Delete category "${cat.name}"?`)) return;
    this.categoryBrandService.deleteCategory(cat.id);
    this.notificationService.success(`Category "${cat.name}" deleted.`);
  }

  openBrandModal(): void {
    this.newBrandName = '';
    this.newBrandOrigin = '';
    this.newBrandStatus = 'PARTNER';
    this.isBrandModalOpen = true;
  }

  closeBrandModal(): void {
    this.isBrandModalOpen = false;
    this.newBrandName = '';
    this.newBrandOrigin = '';
  }

  saveBrand(): void {
    if (!this.newBrandName.trim()) {
      this.notificationService.warning('Brand name cannot be empty');
      return;
    }
    this.categoryBrandService.addBrand(this.newBrandName, this.newBrandOrigin, this.newBrandStatus);
    this.notificationService.success(`Brand "${this.newBrandName.trim()}" added! Available in product creation.`);
    this.closeBrandModal();
  }

  deleteBrand(brand: BrandItem): void {
    if (!confirm(`Delete brand "${brand.name}"?`)) return;
    this.categoryBrandService.deleteBrand(brand.id);
    this.notificationService.success(`Brand "${brand.name}" deleted.`);
  }
}
