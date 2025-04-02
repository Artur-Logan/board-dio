package com.arturlogan.board_dio.dto;

import java.util.List;

public record BoardDetailsDTO(Long id, String name, List<BoardColumnDTO> columnDTOS) {
}
