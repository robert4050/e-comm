import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product, CreateProductRequest } from '../../models/product.model';

@Component({
  selector: 'app-product-form-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-form-modal.component.html',
  styleUrls: ['./product-form-modal.component.css']
})
export class ProductFormModalComponent {
  @Input() isOpen: boolean = false;
  @Input() editingProduct: Product | null = null;
  @Input() categories: string[] = [];
  @Input() brands: string[] = [];

  @Input() form: CreateProductRequest = {
    sku: '',
    name: '',
    description: '',
    price: 99.99,
    category: 'Electronics',
    brand: '',
    stockQuantity: 50,
    imageUrl: ''
  };

  @Output() formSubmitted = new EventEmitter<CreateProductRequest>();
  @Output() modalClosed = new EventEmitter<void>();

  onSubmit(): void {
    this.formSubmitted.emit(this.form);
  }

  onClose(): void {
    this.modalClosed.emit();
  }
}
