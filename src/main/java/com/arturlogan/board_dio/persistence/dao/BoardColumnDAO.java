package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.persistence.entity.BoardColumnEntity;
import com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = "SELECT id, name, \\\"order\\\" FROM BOARDS_COLUMNS WHERE board_id = ? ORDER BY 'order'";

        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()){
                var entity = new BoardColumnEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setOrder(resultSet.getInt("order"));
                entity.setKind(BoardColumnKindEnum.findByName("kind"));
                entities.add(entity);
            }
            return entities;
        }
    }
}
