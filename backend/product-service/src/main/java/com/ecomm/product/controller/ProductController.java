package com.ecomm.product.controller;

import com.ecomm.product.dto.request.CreateProductRequest;
import com.ecomm.product.dto.request.UpdateProductRequest;
import com.ecomm.product.dto.ProductResponse;
import com.ecomm.product.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "search", required = false) String search,
            @PageableDefault(size = 12) Pageable pageable) {
        log.info("Fetching products page: {}, size: {}, category: {}, search: {}",
                pageable.getPageNumber(), pageable.getPageSize(), category, search);
        return ResponseEntity.ok(productService.getProducts(category, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") String id) {
        log.info("Fetching product details for: {}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Received request to create product with SKU: {}", request.getSku());
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("Received request to update product: {}", id);
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ProductResponse> publishProduct(@PathVariable("id") String id) {
        log.info("Received request to publish product: {}", id);
        return ResponseEntity.ok(productService.publishProduct(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") String id) {
        log.info("Received request to delete product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
