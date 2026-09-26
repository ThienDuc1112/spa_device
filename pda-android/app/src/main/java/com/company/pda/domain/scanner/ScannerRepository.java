package com.company.pda.domain.scanner;

import com.company.pda.domain.model.ScanResult;
import java.util.function.Consumer;

public interface ScannerRepository {
  void start(Consumer<ScanResult> onScan, Consumer<String> onError);

  void stop();

  void trigger();
}
