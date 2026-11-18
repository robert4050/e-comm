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
  imageUrl: string;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

export interface Suggestion {
  id: string;
  name: string;
  brand: string;
  category: string;
  price: number;
  imageUrl: string;
  highlightedName?: string;
}

export interface PriceStats {
  min: number;
  max: number;
  avg: number;
}

export interface SearchFacets {
  brands: { [key: string]: number };
  categories: { [key: string]: number };
  priceStats: PriceStats;
  inStockCount: number;
  totalCount: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
