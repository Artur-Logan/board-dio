package com.arturlogan.board_dio.dto;

import com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum;

public record BoardColumnInfoDTO(Long id, int order, BoardColumnKindEnum kind) {
}
