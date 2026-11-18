package com.ecomm.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductResponse {
    private String id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String brand;
    private String status;
    private Integer stockQuantity;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public ProductResponse() {}

    public ProductResponse(String id, String sku, String name, String description, BigDecimal price,
                           String category, String brand, String status, Integer stockQuantity,
                           String imageUrl, Instant createdAt, Instant updatedAt, Long version) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.brand = brand;
        this.status = status;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private String brand;
        private String status;
        private Integer stockQuantity;
        private String imageUrl;
        private Instant createdAt;
        private Instant updatedAt;
        private Long version;

        public Builder id(String id) { this.id = id; return this; }
        public Builder sku(String sku) { this.sku = sku; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder brand(String brand) { this.brand = brand; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder stockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder version(Long version) { this.version = version; return this; }

        public ProductResponse build() {
            return new ProductResponse(id, sku, name, description, price, category, brand, status, stockQuantity, imageUrl, createdAt, updatedAt, version);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
