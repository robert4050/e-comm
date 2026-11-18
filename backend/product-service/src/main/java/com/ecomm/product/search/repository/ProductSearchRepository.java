package com.ecomm.product.search.repository;

import com.ecomm.product.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"brand^2\", \"description\", \"category\"], \"fuzziness\": \"AUTO\"}}")
    Page<ProductDocument> searchByFuzzyQuery(String query, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"category\": \"?0\"}}, {\"multi_match\": {\"query\": \"?1\", \"fields\": [\"name^3\", \"brand^2\", \"description\"], \"fuzziness\": \"AUTO\"}}]}}")
    Page<ProductDocument> searchByCategoryAndFuzzyQuery(String category, String query, Pageable pageable);
}
