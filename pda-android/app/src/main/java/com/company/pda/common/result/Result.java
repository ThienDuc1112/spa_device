package com.company.pda.common.result;

public final class Result<T> {
  private final T data;
  private final boolean cached;

  public Result(T data, boolean cached) {
    this.data = data;
    this.cached = cached;
  }

  public T data() {
    return data;
  }

  public boolean cached() {
    return cached;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Result<?> other)) return false;
    return java.util.Objects.equals(data, other.data)
        && java.util.Objects.equals(cached, other.cached);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(data, cached);
  }

  @Override
  public String toString() {
    return "Result[data=" + data + ", cached=" + cached + "]";
  }
}
