package com.ecomm.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.ecomm"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@EnableCaching
@EntityScan(basePackages = {"com.ecomm.product.entity"})
@EnableJpaRepositories(basePackages = {"com.ecomm.product.repository"})
@EnableElasticsearchRepositories(basePackages = {"com.ecomm.product.search.repository"})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
