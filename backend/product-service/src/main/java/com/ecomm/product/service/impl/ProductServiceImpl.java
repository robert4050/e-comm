package com.ecomm.product.service.impl;

import com.ecomm.product.document.ProductDocument;
import com.ecomm.product.dto.request.CreateProductRequest;
import com.ecomm.product.dto.request.UpdateProductRequest;
import com.ecomm.product.dto.ProductResponse;
import com.ecomm.product.entity.Product;
import com.ecomm.product.exception.ProductAlreadyExistsException;
import com.ecomm.product.exception.ProductNotFoundException;
import com.ecomm.product.kafka.ProductEventPublisher;
import com.ecomm.product.repository.ProductRepository;
import com.ecomm.product.search.repository.ProductSearchRepository;
import com.ecomm.product.search.service.ProductSearchService;
import com.ecomm.product.search.service.ProductSearchSyncService;
import com.ecomm.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductEventPublisher eventPublisher;
    private final ProductSearchRepository productSearchRepository;
    private final ProductSearchSyncService searchSyncService;
    private final ProductSearchService productSearchService;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductEventPublisher eventPublisher,
                              ProductSearchRepository productSearchRepository,
                              ProductSearchSyncService searchSyncService,
                              ProductSearchService productSearchService) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.productSearchRepository = productSearchRepository;
        this.searchSyncService = searchSyncService;
        this.productSearchService = productSearchService;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new ProductAlreadyExistsException(request.getSku());
        }

        Product product = Product.builder()
                .id("prod-" + UUID.randomUUID().toString().substring(0, 8))
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .brand(request.getBrand() != null && !request.getBrand().isBlank() ? request.getBrand() : null)
                .status("PUBLISHED")
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl() != null && !request.getImageUrl().isBlank()
                        ? request.getImageUrl()
                        : "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600")
                .build();

        Product saved = productRepository.save(product);
        searchSyncService.indexProduct(saved);
        ProductResponse response = mapToResponse(saved);

        eventPublisher.saveToOutbox("ProductCreated", response);
        log.info("Created product with ID: {}, SKU: {}, Brand: {}", saved.getId(), saved.getSku(), saved.getBrand());
        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        product.setStockQuantity(request.getStockQuantity());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            product.setImageUrl(request.getImageUrl());
        }

        Product updated = productRepository.save(product);
        searchSyncService.indexProduct(updated);
        ProductResponse response = mapToResponse(updated);

        eventPublisher.saveToOutbox("ProductUpdated", response);
        log.info("Updated product with ID: {}, Brand: {}", updated.getId(), updated.getBrand());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        log.info("Fetching product details from MySQL database for ID: {}", id);
        return productRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String category, String query, Pageable pageable) {
        // 1. If search query is provided, execute high-speed fuzzy search via Elasticsearch!
        if (query != null && !query.isBlank()) {
            try {
                Page<ProductDocument> searchPage;
                if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
                    searchPage = productSearchRepository.searchByCategoryAndFuzzyQuery(category, query.trim(), pageable);
                } else {
                    searchPage = productSearchRepository.searchByFuzzyQuery(query.trim(), pageable);
                }
                log.info("Elasticsearch fuzzy query '{}' returned {} results", query.trim(), searchPage.getTotalElements());
                return searchPage.map(this::mapDocumentToResponse);
            } catch (Exception e) {
                log.warn("Elasticsearch search failed, falling back gracefully to MySQL: {}", e.getMessage());
            }
        }

        // 2. Fallback or standard category browsing via MySQL JPA
        Page<Product> page;
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
            page = productRepository.findByCategoryIgnoreCase(category, pageable);
        } else if (query != null && !query.isBlank()) {
            page = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsFiltered(String category, String brand, String query,
                                                     BigDecimal minPrice, BigDecimal maxPrice,
                                                     Boolean inStock, String sortBy, Pageable pageable) {
        // If advanced filters are present, use the full Elasticsearch engine
        if ((brand != null && !brand.isBlank() && !brand.equalsIgnoreCase("All")) ||
            minPrice != null || maxPrice != null || Boolean.TRUE.equals(inStock) ||
            (sortBy != null && !sortBy.isBlank() && !sortBy.equalsIgnoreCase("default"))) {
            try {
                Page<ProductResponse> results = productSearchService.searchAdvanced(category, brand, query, minPrice, maxPrice, inStock, sortBy, pageable);
                if (results != null) {
                    return results;
                }
            } catch (Exception e) {
                log.warn("Advanced Elasticsearch query failed, falling back to database: {}", e.getMessage());
            }
        }
        return getProducts(category, query, pageable);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse publishProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setStatus("PUBLISHED");
        Product saved = productRepository.save(product);
        searchSyncService.indexProduct(saved);
        ProductResponse response = mapToResponse(saved);

        eventPublisher.saveToOutbox("ProductPublished", response);
        log.info("Published product with ID: {}", saved.getId());
        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        searchSyncService.deleteProduct(id);
        log.info("Deleted product with ID: {}", id);
    }

    private ProductResponse mapDocumentToResponse(ProductDocument doc) {
        return ProductResponse.builder()
                .id(doc.getId())
                .sku(doc.getSku())
                .name(doc.getName())
                .description(doc.getDescription())
                .price(doc.getPrice())
                .category(doc.getCategory())
                .brand(doc.getBrand())
                .status(doc.getStatus())
                .stockQuantity(doc.getStockQuantity())
                .imageUrl(doc.getImageUrl())
                .version(1L)
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .brand(product.getBrand())
                .status(product.getStatus())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .version(product.getVersion())
                .build();
    }
}
