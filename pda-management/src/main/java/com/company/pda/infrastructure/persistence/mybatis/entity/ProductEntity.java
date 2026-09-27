package com.company.pda.infrastructure.persistence.mybatis.entity;

public record ProductEntity(
    long id,
    String barcode,
    String productCode,
    String productName,
    String imageUrl,
    long imageVersion) {}
