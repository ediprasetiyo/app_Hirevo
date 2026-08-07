package com.hirevo.core.pagination;

import java.util.List;

/** Envelope for cursor-based paginated responses. */
public record CursorPage<T>(List<T> data, Pagination pagination) {

  public record Pagination(String nextCursor, String prevCursor, boolean hasMore, Long total) {}

  public static <T> CursorPage<T> of(List<T> data, String nextCursor, boolean hasMore) {
    return new CursorPage<>(data, new Pagination(nextCursor, null, hasMore, null));
  }

  public static <T> CursorPage<T> empty() {
    return new CursorPage<>(List.of(), new Pagination(null, null, false, 0L));
  }
}
