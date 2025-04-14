package com.arturlogan.board_dio.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public record BoardDetailsDTO( Long id,
        String name,
        List<BoardColumnDTO> columns) {
}