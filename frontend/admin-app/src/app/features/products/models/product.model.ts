export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  category: string;
  brand?: string;
  status: string;
  stockQuantity: number;
  imageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  description: string;
  price: number;
  category: string;
  brand?: string;
  stockQuantity: number;
  imageUrl?: string;
}

export interface UpdateProductRequest {
  name: string;
  description: string;
  price: number;
  category: string;
  brand?: string;
  stockQuantity: number;
  imageUrl?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
