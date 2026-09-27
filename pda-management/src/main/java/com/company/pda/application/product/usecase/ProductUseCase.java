package com.company.pda.application.product.usecase;

import com.company.pda.application.product.dto.ImageSyncCommand;

public interface ProductUseCase extends GetProductByBarcodeUseCase {
  void sync(ImageSyncCommand body);
}
