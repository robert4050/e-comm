package com.ecomm.product.search.service;

import com.ecomm.product.document.ProductDocument;
import com.ecomm.product.entity.Product;
import com.ecomm.product.repository.ProductRepository;
import com.ecomm.product.search.repository.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchSyncService {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchSyncService.class);

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    public ProductSearchSyncService(ProductRepository productRepository,
                                    ProductSearchRepository productSearchRepository) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncAllProductsOnStartup() {
        try {
            log.info("Starting initial Elasticsearch product catalog synchronization...");
            List<Product> allProducts = productRepository.findAll();
            if (allProducts.isEmpty()) {
                log.info("No products found in MySQL database to sync with Elasticsearch.");
                return;
            }

            List<ProductDocument> documents = allProducts.stream()
                    .map(this::mapToDocument)
                    .collect(Collectors.toList());

            productSearchRepository.saveAll(documents);
            log.info("Successfully indexed {} products into Elasticsearch index 'products'", documents.size());
        } catch (Exception e) {
            log.warn("Failed to synchronize products with Elasticsearch at startup (Elasticsearch may still be starting): {}", e.getMessage());
        }
    }

    public void indexProduct(Product product) {
        try {
            ProductDocument doc = mapToDocument(product);
            productSearchRepository.save(doc);
            log.info("Indexed product {} into Elasticsearch", product.getId());
        } catch (Exception e) {
            log.warn("Error indexing product {} into Elasticsearch: {}", product.getId(), e.getMessage());
        }
    }

    public void deleteProduct(String id) {
        try {
            productSearchRepository.deleteById(id);
            log.info("Deleted product {} from Elasticsearch", id);
        } catch (Exception e) {
            log.warn("Error deleting product {} from Elasticsearch: {}", id, e.getMessage());
        }
    }

    public ProductDocument mapToDocument(Product product) {
        return ProductDocument.builder()
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
                .build();
    }
}
