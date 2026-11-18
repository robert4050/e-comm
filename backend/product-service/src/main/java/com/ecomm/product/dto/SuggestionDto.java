package com.ecomm.product.dto;

import java.math.BigDecimal;

public class SuggestionDto {

    private String id;
    private String name;
    private String brand;
    private String category;
    private BigDecimal price;
    private String imageUrl;
    private String highlightedName;

    public SuggestionDto() {}

    public SuggestionDto(String id, String name, String brand, String category,
                         BigDecimal price, String imageUrl, String highlightedName) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.highlightedName = highlightedName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getHighlightedName() { return highlightedName; }
    public void setHighlightedName(String highlightedName) { this.highlightedName = highlightedName; }
}
