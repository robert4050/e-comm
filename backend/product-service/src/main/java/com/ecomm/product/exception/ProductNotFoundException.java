package com.ecomm.product.exception;

import com.ecomm.common.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(String productId) {
        super("Product", productId);
    }
}
