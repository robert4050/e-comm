package com.ecomm.product.search.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import com.ecomm.product.document.ProductDocument;
import com.ecomm.product.dto.ProductResponse;
import com.ecomm.product.dto.SearchFacetsResponse;
import com.ecomm.product.dto.SuggestionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchService.class);

    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    private static final java.util.regex.Pattern BETWEEN_PRICE_PATTERN =
            java.util.regex.Pattern.compile("(?i)\\bbetween\\s+\\$?(\\d+(?:\\.\\d+)?)\\s+(?:and|-)\\s+\\$?(\\d+(?:\\.\\d+)?)\\b");

    private static final java.util.regex.Pattern UNDER_PRICE_PATTERN =
            java.util.regex.Pattern.compile("(?i)\\b(under|below|less than|cheaper than|max|up to|<)\\s*\\$?(\\d+(?:\\.\\d+)?)\\b");

    private static final java.util.regex.Pattern ABOVE_PRICE_PATTERN =
            java.util.regex.Pattern.compile("(?i)\\b(above|over|more than|min|starting from|>)\\s*\\$?(\\d+(?:\\.\\d+)?)\\b");

    public static class QueryIntent {
        private final String cleanQuery;
        private final BigDecimal minPrice;
        private final BigDecimal maxPrice;

        public QueryIntent(String cleanQuery, BigDecimal minPrice, BigDecimal maxPrice) {
            this.cleanQuery = cleanQuery;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }

        public String getCleanQuery() { return cleanQuery; }
        public BigDecimal getMinPrice() { return minPrice; }
        public BigDecimal getMaxPrice() { return maxPrice; }
        public boolean hasPriceFilter() { return minPrice != null || maxPrice != null; }
    }

    public QueryIntent parseQueryIntent(String query) {
        if (query == null || query.isBlank()) {
            return new QueryIntent("", null, null);
        }

        String working = query.trim();
        BigDecimal min = null;
        BigDecimal max = null;

        // 1. Between X and Y
        var betweenMatcher = BETWEEN_PRICE_PATTERN.matcher(working);
        if (betweenMatcher.find()) {
            try {
                min = new BigDecimal(betweenMatcher.group(1));
                max = new BigDecimal(betweenMatcher.group(2));
                working = betweenMatcher.replaceFirst("").trim();
            } catch (Exception ignored) {}
        }

        // 2. Under / Below X
        var underMatcher = UNDER_PRICE_PATTERN.matcher(working);
        if (underMatcher.find()) {
            try {
                max = new BigDecimal(underMatcher.group(2));
                working = underMatcher.replaceFirst("").trim();
            } catch (Exception ignored) {}
        }

        // 3. Above / Over X
        var aboveMatcher = ABOVE_PRICE_PATTERN.matcher(working);
        if (aboveMatcher.find()) {
            try {
                min = new BigDecimal(aboveMatcher.group(2));
                working = aboveMatcher.replaceFirst("").trim();
            } catch (Exception ignored) {}
        }

        working = working.replaceAll("\\s+", " ").trim();
        return new QueryIntent(working, min, max);
    }

    /**
     * Instant search-as-you-type suggestions with natural language price intent matching
     */
    public List<SuggestionDto> getSuggestions(String prefix) {
        if (prefix == null || prefix.trim().isBlank()) {
            return Collections.emptyList();
        }

        QueryIntent intent = parseQueryIntent(prefix);
        String cleanText = intent.getCleanQuery();
        if (cleanText.isBlank()) {
            cleanText = prefix.trim();
        }
        final String effectiveText = cleanText;

        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        b.should(s -> s.matchPhrasePrefix(mpp -> mpp.field("name").query(effectiveText).boost(4.0f)));
                        b.should(s -> s.prefix(p -> p.field("brand").value(effectiveText.toLowerCase()).boost(3.0f)));
                        b.should(s -> s.multiMatch(mm -> mm
                                .query(effectiveText)
                                .fields("name^3", "brand^2", "category", "description")
                                .fuzziness("AUTO")
                                .boost(2.0f)));

                        if (intent.getMinPrice() != null || intent.getMaxPrice() != null) {
                            b.filter(f -> f.range(r -> {
                                r.field("price");
                                if (intent.getMinPrice() != null) r.gte(JsonData.of(intent.getMinPrice().doubleValue()));
                                if (intent.getMaxPrice() != null) r.lte(JsonData.of(intent.getMaxPrice().doubleValue()));
                                return r;
                            }));
                        }
                        b.minimumShouldMatch("1");
                        return b;
                    }))
                    .withPageable(PageRequest.of(0, 6))
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

            List<SuggestionDto> suggestions = new ArrayList<>();

            for (SearchHit<ProductDocument> hit : searchHits.getSearchHits()) {
                ProductDocument doc = hit.getContent();
                suggestions.add(new SuggestionDto(
                        doc.getId(),
                        doc.getName(),
                        doc.getBrand(),
                        doc.getCategory(),
                        doc.getPrice(),
                        doc.getImageUrl(),
                        doc.getName()
                ));
            }

            return suggestions;

        } catch (Exception e) {
            log.warn("Elasticsearch suggestions failed for '{}': {}", prefix, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Computes real-time dynamic facets (brand counts, category counts, price stats) in a single request
     */
    public SearchFacetsResponse getFacets(String category, String query) {
        try {
            NativeQueryBuilder queryBuilder = NativeQuery.builder().withMaxResults(0);

            QueryIntent intent = parseQueryIntent(query);
            String effectiveQuery = intent.getCleanQuery();

            // Optional filters if user is already browsing inside a category or query
            if ((category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) ||
                (effectiveQuery != null && !effectiveQuery.isBlank()) ||
                intent.hasPriceFilter()) {
                queryBuilder.withQuery(q -> q.bool(b -> {
                    if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
                        b.filter(f -> f.term(t -> t.field("category").value(category)));
                    }
                    if (effectiveQuery != null && !effectiveQuery.isBlank()) {
                        b.must(m -> m.multiMatch(mm -> mm.query(effectiveQuery.trim())
                                .fields("name^3", "brand^2", "description", "category")
                                .fuzziness("AUTO")));
                    }
                    if (intent.getMinPrice() != null || intent.getMaxPrice() != null) {
                        b.filter(f -> f.range(r -> {
                            r.field("price");
                            if (intent.getMinPrice() != null) r.gte(JsonData.of(intent.getMinPrice().doubleValue()));
                            if (intent.getMaxPrice() != null) r.lte(JsonData.of(intent.getMaxPrice().doubleValue()));
                            return r;
                        }));
                    }
                    return b;
                }));
            }

            // Aggregations
            queryBuilder.withAggregation("brands", Aggregation.of(a -> a.terms(t -> t.field("brand").size(30))));
            queryBuilder.withAggregation("categories", Aggregation.of(a -> a.terms(t -> t.field("category").size(20))));
            queryBuilder.withAggregation("price_stats", Aggregation.of(a -> a.stats(s -> s.field("price"))));

            NativeQuery nativeQuery = queryBuilder.build();
            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

            Map<String, Long> brands = new LinkedHashMap<>();
            Map<String, Long> categories = new LinkedHashMap<>();
            SearchFacetsResponse.PriceStats priceStats = new SearchFacetsResponse.PriceStats(0.0, 0.0, 0.0);

            if (searchHits.hasAggregations()) {
                var aggregations = (org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations) searchHits.getAggregations();

                // 1. Brand counts
                var brandAgg = aggregations.get("brands");
                if (brandAgg != null && brandAgg.aggregation() != null && brandAgg.aggregation().getAggregate().isSterms()) {
                    for (StringTermsBucket bucket : brandAgg.aggregation().getAggregate().sterms().buckets().array()) {
                        if (bucket.key().stringValue() != null && !bucket.key().stringValue().isBlank()) {
                            brands.put(bucket.key().stringValue(), bucket.docCount());
                        }
                    }
                }

                // 2. Category counts
                var catAgg = aggregations.get("categories");
                if (catAgg != null && catAgg.aggregation() != null && catAgg.aggregation().getAggregate().isSterms()) {
                    for (StringTermsBucket bucket : catAgg.aggregation().getAggregate().sterms().buckets().array()) {
                        if (bucket.key().stringValue() != null && !bucket.key().stringValue().isBlank()) {
                            categories.put(bucket.key().stringValue(), bucket.docCount());
                        }
                    }
                }

                // 3. Price stats
                var priceAgg = aggregations.get("price_stats");
                if (priceAgg != null && priceAgg.aggregation() != null && priceAgg.aggregation().getAggregate().isStats()) {
                    var stats = priceAgg.aggregation().getAggregate().stats();
                    priceStats = new SearchFacetsResponse.PriceStats(
                            stats.min(),
                            stats.max(),
                            stats.avg()
                    );
                }
            }

            return new SearchFacetsResponse(brands, categories, priceStats, searchHits.getTotalHits(), searchHits.getTotalHits());

        } catch (Exception e) {
            log.warn("Error computing Elasticsearch facets: {}", e.getMessage());
            return new SearchFacetsResponse(Collections.emptyMap(), Collections.emptyMap(), new SearchFacetsResponse.PriceStats(0.0, 0.0, 0.0), 0L, 0L);
        }
    }

    /**
     * Advanced Multi-Criteria Search (Query + Category + Brand + Price Range + InStock + Sorting)
     */
    public Page<ProductResponse> searchAdvanced(String category, String brand, String query,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               Boolean inStockOnly, String sortBy,
                                               Pageable pageable) {
        try {
            Pageable cleanPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            NativeQueryBuilder queryBuilder = NativeQuery.builder().withPageable(cleanPageable);

            // Natural language price intent extraction (e.g. "laptop under 1000" -> clean: "laptop", maxPrice: 1000)
            QueryIntent intent = parseQueryIntent(query);
            String effectiveQuery = intent.getCleanQuery();
            BigDecimal effectiveMinPrice = minPrice != null ? minPrice : intent.getMinPrice();
            BigDecimal effectiveMaxPrice = maxPrice != null ? maxPrice : intent.getMaxPrice();

            // Highlighting configuration: wrap matching words with <mark> if query is present
            if (effectiveQuery != null && !effectiveQuery.trim().isBlank()) {
                queryBuilder.withHighlightQuery(new HighlightQuery(
                        new Highlight(List.of(
                                new HighlightField("name"),
                                new HighlightField("description")
                        )),
                        ProductDocument.class
                ));
            }

            // Bool Query Construction
            queryBuilder.withQuery(q -> q.bool(b -> {
                // Fuzzy query if keyword provided
                if (effectiveQuery != null && !effectiveQuery.trim().isBlank()) {
                    b.must(m -> m.multiMatch(mm -> mm
                            .query(effectiveQuery.trim())
                            .fields("name^3", "brand^2", "description", "category")
                            .fuzziness("AUTO")));
                }

                // Category filter
                if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
                    b.filter(f -> f.term(t -> t.field("category").value(category)));
                }

                // Brand filter
                if (brand != null && !brand.isBlank() && !brand.equalsIgnoreCase("All")) {
                    b.filter(f -> f.term(t -> t.field("brand").value(brand)));
                }

                // Price range filter (combines explicit filters + natural language intent)
                if (effectiveMinPrice != null || effectiveMaxPrice != null) {
                    b.filter(f -> f.range(r -> {
                        r.field("price");
                        if (effectiveMinPrice != null) r.gte(JsonData.of(effectiveMinPrice.doubleValue()));
                        if (effectiveMaxPrice != null) r.lte(JsonData.of(effectiveMaxPrice.doubleValue()));
                        return r;
                    }));
                }

                // In-stock only filter
                if (Boolean.TRUE.equals(inStockOnly)) {
                    b.filter(f -> f.range(r -> r.field("stockQuantity").gt(JsonData.of(0))));
                }

                return b;
            }));

            // Sort Configuration
            if ("price_asc".equalsIgnoreCase(sortBy)) {
                queryBuilder.withSort(s -> s.field(f -> f.field("price").order(SortOrder.Asc)));
            } else if ("price_desc".equalsIgnoreCase(sortBy)) {
                queryBuilder.withSort(s -> s.field(f -> f.field("price").order(SortOrder.Desc)));
            } else if ("name_asc".equalsIgnoreCase(sortBy)) {
                queryBuilder.withSort(s -> s.field(f -> f.field("name.keyword").order(SortOrder.Asc)));
            }
            // default is relevance score (_score)

            NativeQuery nativeQuery = queryBuilder.build();
            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

            List<ProductResponse> responseList = searchHits.getSearchHits().stream().map(hit -> {
                ProductDocument doc = hit.getContent();
                String highlightedDescription = doc.getDescription();

                // If description had highlighted match, use it!
                if (hit.getHighlightFields().containsKey("description")) {
                    List<String> highlights = hit.getHighlightField("description");
                    if (!highlights.isEmpty()) {
                        highlightedDescription = highlights.get(0);
                    }
                }

                return ProductResponse.builder()
                        .id(doc.getId())
                        .sku(doc.getSku())
                        .name(doc.getName())
                        .description(highlightedDescription)
                        .price(doc.getPrice())
                        .category(doc.getCategory())
                        .brand(doc.getBrand())
                        .status(doc.getStatus())
                        .stockQuantity(doc.getStockQuantity())
                        .imageUrl(doc.getImageUrl())
                        .version(1L)
                        .build();
            }).collect(Collectors.toList());

            return new PageImpl<>(responseList, pageable, searchHits.getTotalHits());

        } catch (Exception e) {
            log.warn("Advanced Elasticsearch query failed, falling back: {}", e.getMessage());
            return Page.empty(pageable);
        }
    }
}
