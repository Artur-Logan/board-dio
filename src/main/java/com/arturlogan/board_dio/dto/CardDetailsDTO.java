package com.arturlogan.board_dio.dto;

import java.time.OffsetDateTime;

public record CardDetailsDTO(Long id,
                             String title,
                             String description,
                             boolean blocked,
                             OffsetDateTime blockAt,
                             String blockReason,
                             int blocksAmount,
                             Long columnId,
                             String columnName)
 {
}
