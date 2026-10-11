package com.company.pda.presentation.disposal;

import com.company.pda.data.remote.dto.disposal.DisposalDto;

public final class DisposalUiState {
  private final java.util.List<DisposalDto> list;
  private final DisposalDto.Detail detail;
  private final int page;

  public DisposalUiState(java.util.List<DisposalDto> list, DisposalDto.Detail detail, int page) {
    this.list = list;
    this.detail = detail;
    this.page = page;
  }

  public java.util.List<DisposalDto> list() {
    return list;
  }

  public DisposalDto.Detail detail() {
    return detail;
  }

  public int page() {
    return page;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DisposalUiState other)) return false;
    return java.util.Objects.equals(list, other.list)
        && java.util.Objects.equals(detail, other.detail)
        && java.util.Objects.equals(page, other.page);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(list, detail, page);
  }

  @Override
  public String toString() {
    return "DisposalUiState[list=" + list + ", detail=" + detail + ", page=" + page + "]";
  }
}
