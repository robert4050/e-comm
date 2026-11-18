import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, PageResponse, Suggestion, SearchFacets } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/products';

  getProducts(
    category?: string,
    search?: string,
    page: number = 0,
    size: number = 12,
    brand?: string,
    minPrice?: number,
    maxPrice?: number,
    inStock?: boolean,
    sortBy?: string
  ): Observable<PageResponse<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (category && category !== 'All') {
      params = params.set('category', category);
    }
    if (search && search.trim() !== '') {
      params = params.set('search', search.trim());
    }
    if (brand && brand !== 'All') {
      params = params.set('brand', brand);
    }
    if (minPrice !== undefined && minPrice !== null) {
      params = params.set('minPrice', minPrice.toString());
    }
    if (maxPrice !== undefined && maxPrice !== null) {
      params = params.set('maxPrice', maxPrice.toString());
    }
    if (inStock !== undefined && inStock !== null) {
      params = params.set('inStock', inStock.toString());
    }
    if (sortBy && sortBy !== 'default') {
      params = params.set('sortBy', sortBy);
    }

    return this.http.get<PageResponse<Product>>(this.baseUrl, { params });
  }

  getSuggestions(prefix: string): Observable<Suggestion[]> {
    const params = new HttpParams().set('q', prefix.trim());
    return this.http.get<Suggestion[]>(`${this.baseUrl}/suggest`, { params });
  }

  getFacets(category?: string, search?: string): Observable<SearchFacets> {
    let params = new HttpParams();
    if (category && category !== 'All') {
      params = params.set('category', category);
    }
    if (search && search.trim() !== '') {
      params = params.set('search', search.trim());
    }
    return this.http.get<SearchFacets>(`${this.baseUrl}/facets`, { params });
  }

  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }
}
