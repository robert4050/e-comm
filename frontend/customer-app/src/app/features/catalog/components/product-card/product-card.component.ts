import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product } from '../../models/product.model';
import { PriceFormatPipe } from '../../../../shared/pipes/price-format.pipe';
import { TruncatePipe } from '../../../../shared/pipes/truncate.pipe';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, PriceFormatPipe, TruncatePipe],
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.css']
})
export class ProductCardComponent {
  @Input({ required: true }) product!: Product;
  @Output() addToCartClicked = new EventEmitter<Product>();

  onAddToCart(): void {
    this.addToCartClicked.emit(this.product);
  }
}
