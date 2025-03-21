package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.persistence.entity.BoardColumnEntity;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class BoardColumnDAO {

    private final Connection connection;

    public BoardColumnEntity insert(final BoardColumnEntity boardColumn) throws SQLException{
        var sql = "INSERT INTO BOARDS_COLUMNS (name,  \\\"order\\\", kind, board_id) VALUES (?, ?, ?, ?);";
        try(var preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, boardColumn.getName());
            preparedStatement.setInt(2, boardColumn.getOrder());
            preparedStatement.setString(3, boardColumn.getKind().name());
            preparedStatement.setLong(4, boardColumn.getBoard().getId());

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    boardColumn.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Falha ao obter o ID gerado.");
                }
            }
        }
        return boardColumn;
    }

    public List<BoardColumnEntity> findByBoardId(Long id) throws SQLException {
        return null;
    }
}
