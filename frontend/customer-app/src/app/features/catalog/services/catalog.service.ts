import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, PageResponse } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/products';

  getProducts(category?: string, search?: string, page: number = 0, size: number = 12): Observable<PageResponse<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (category && category !== 'All') {
      params = params.set('category', category);
    }
    if (search && search.trim() !== '') {
      params = params.set('search', search.trim());
    }

    return this.http.get<PageResponse<Product>>(this.baseUrl, { params });
  }

  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }
}
