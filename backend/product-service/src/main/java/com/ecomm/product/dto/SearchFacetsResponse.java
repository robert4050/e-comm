package com.ecomm.product.dto;

import java.util.Map;

public class SearchFacetsResponse {

    private Map<String, Long> brands;
    private Map<String, Long> categories;
    private PriceStats priceStats;
    private Long inStockCount;
    private Long totalCount;

    public SearchFacetsResponse() {}

    public SearchFacetsResponse(Map<String, Long> brands, Map<String, Long> categories,
                                PriceStats priceStats, Long inStockCount, Long totalCount) {
        this.brands = brands;
        this.categories = categories;
        this.priceStats = priceStats;
        this.inStockCount = inStockCount;
        this.totalCount = totalCount;
    }

    public static class PriceStats {
        private Double min;
        private Double max;
        private Double avg;

        public PriceStats() {}

        public PriceStats(Double min, Double max, Double avg) {
            this.min = min;
            this.max = max;
            this.avg = avg;
        }

        public Double getMin() { return min; }
        public void setMin(Double min) { this.min = min; }

        public Double getMax() { return max; }
        public void setMax(Double max) { this.max = max; }

        public Double getAvg() { return avg; }
        public void setAvg(Double avg) { this.avg = avg; }
    }

    public Map<String, Long> getBrands() { return brands; }
    public void setBrands(Map<String, Long> brands) { this.brands = brands; }

    public Map<String, Long> getCategories() { return categories; }
    public void setCategories(Map<String, Long> categories) { this.categories = categories; }

    public PriceStats getPriceStats() { return priceStats; }
    public void setPriceStats(PriceStats priceStats) { this.priceStats = priceStats; }

    public Long getInStockCount() { return inStockCount; }
    public void setInStockCount(Long inStockCount) { this.inStockCount = inStockCount; }

    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
}
