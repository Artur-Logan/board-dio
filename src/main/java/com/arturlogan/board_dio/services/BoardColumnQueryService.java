package com.arturlogan.board_dio.services;

import com.arturlogan.board_dio.persistence.dao.BoardColumnDAO;
import com.arturlogan.board_dio.persistence.entity.BoardColumnEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardColumnQueryService {

    private final Connection connection;

    public Optional<BoardColumnEntity> findById(final Long id) throws SQLException {
        var dao = new BoardColumnDAO(connection);

        return dao.findById(id);
    }
}
