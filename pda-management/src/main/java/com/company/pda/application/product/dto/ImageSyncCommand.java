package com.company.pda.application.product.dto;

public record ImageSyncCommand(String productCode, String imageUrl, long sourceVersion) {}
