package com.ecomm.product.exception;

import com.ecomm.common.exception.ConflictException;

public class ProductAlreadyExistsException extends ConflictException {

    public ProductAlreadyExistsException(String sku) {
        super("Product", "SKU", sku);
    }
}
