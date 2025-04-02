package com.arturlogan.board_dio.dto;

import com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum;

public record BoardColumnDTO(Long id, String name, BoardColumnKindEnum kindEnum, int cardsAmount) {
}
