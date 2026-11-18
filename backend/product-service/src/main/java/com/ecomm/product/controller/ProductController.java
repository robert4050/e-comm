package com.ecomm.product.controller;

import com.ecomm.product.dto.SearchFacetsResponse;
import com.ecomm.product.dto.SuggestionDto;
import com.ecomm.product.dto.request.CreateProductRequest;
import com.ecomm.product.dto.request.UpdateProductRequest;
import com.ecomm.product.dto.ProductResponse;
import com.ecomm.product.search.service.ProductSearchService;
import com.ecomm.product.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ProductSearchService searchService;

    public ProductController(ProductService productService, ProductSearchService searchService) {
        this.productService = productService;
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "inStock", required = false) Boolean inStock,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @PageableDefault(size = 12) Pageable pageable) {
        String effectiveSort = (sortBy != null && !sortBy.isBlank()) ? sortBy : sort;
        Pageable cleanPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        log.info("Fetching products page: {}, category: {}, brand: {}, search: {}, sort: {}",
                pageable.getPageNumber(), category, brand, search, effectiveSort);
        return ResponseEntity.ok(productService.getProductsFiltered(category, brand, search, minPrice, maxPrice, inStock, effectiveSort, cleanPageable));
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<SuggestionDto>> getSuggestions(@RequestParam(name = "q", required = false) String q) {
        log.info("Fetching autocomplete search suggestions for prefix: '{}'", q);
        return ResponseEntity.ok(searchService.getSuggestions(q));
    }

    @GetMapping("/facets")
    public ResponseEntity<SearchFacetsResponse> getFacets(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "search", required = false) String search) {
        log.info("Computing dynamic Elasticsearch facets for category: '{}', search: '{}'", category, search);
        return ResponseEntity.ok(searchService.getFacets(category, search));
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
