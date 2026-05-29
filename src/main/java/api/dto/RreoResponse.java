package api.dto;

import java.util.List;

public record RreoResponse(
      List<RreoItem> items,
      boolean hasMore,
      int count
) {}
