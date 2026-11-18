package com.ecomm.product.service;

import com.ecomm.product.dto.request.CreateProductRequest;
import com.ecomm.product.dto.request.UpdateProductRequest;
import com.ecomm.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(String id, UpdateProductRequest request);
    ProductResponse getProductById(String id);
    Page<ProductResponse> getProducts(String category, String query, Pageable pageable);
    ProductResponse publishProduct(String id);
    void deleteProduct(String id);
}
