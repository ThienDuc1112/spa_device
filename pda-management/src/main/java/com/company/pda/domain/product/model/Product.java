package com.company.pda.domain.product.model;

public record Product(
    long id,
    String barcode,
    String productCode,
    String productName,
    String imageUrl,
    long imageVersion) {}
