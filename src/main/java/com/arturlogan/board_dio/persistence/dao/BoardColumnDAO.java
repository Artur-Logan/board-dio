package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.dto.BoardColumnDTO;
import com.arturlogan.board_dio.persistence.entity.BoardColumnEntity;
import com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum;
import com.arturlogan.board_dio.persistence.entity.CardEntity;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum.findByName;

@RequiredArgsConstructor
public class BoardColumnDAO {

    private final Connection connection;

    public BoardColumnEntity insert(final BoardColumnEntity boardColumn) throws SQLException {
        var sql = "INSERT INTO BOARDS_COLUMNS (name, \"order\", kind, board_id) VALUES (?, ?, ?, ?);";
        try (var preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    public List<BoardColumnEntity> findByBoardId(Long boardId) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = "SELECT id, name, \"order\", kind FROM BOARDS_COLUMNS WHERE board_id = ? ORDER BY \"order\""; // Correção aqui

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()) {
                var entity = new BoardColumnEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setOrder(resultSet.getInt("order"));
                entity.setKind(findByName(resultSet.getString("kind")));
                entities.add(entity);
            }
            return entities;
        }
    }

    public List<BoardColumnDTO> findByBoardIdWithDetails(Long boardId) throws SQLException {
        List<BoardColumnDTO> dtos = new ArrayList<>();
        var sql = "SELECT bc.id, bc.name, bc.kind, COUNT(SELECT c.id FROM CARDS c WHERE c.board_column_id = bc.id) cards_amount FROM BOARDS_COLUMNS bc WHERE board_id = ? ORDER BY \"order\""; // Correção aqui

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()) {
                var dto = new BoardColumnDTO(
                        resultSet.getLong("bc.id"),
                        resultSet.getString("bc.name"),
                        findByName(resultSet.getString("bc.kind")),
                        resultSet.getInt("cards_amount")
                );
                dtos.add(dto);
            }
            return dtos;
        }
    }

    public Optional<BoardColumnEntity> findById(Long boardId) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = "SELECT bc.name, bc.kind, c.id, c.title, c.description FROM BOARDS_COLUMNS bc INNER JOIN CARDS c ON c.board_column_id = bc.id WHERE bc.id = ?";

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()) {
                var entity = new BoardColumnEntity();
                entity.setName(resultSet.getString("bc.name"));
                entity.setKind(findByName(resultSet.getString("bc.kind")));
                do {
                    var card = new CardEntity();
                    card.setId(resultSet.getLong("c.id"));
                    card.setTitle(resultSet.getString("c.title"));
                    card.setDescription(resultSet.getString("c.description"));
                    entity.getCards().add(card);
                } while (resultSet.next());
            }
            return Optional.empty();
        }
    }
}
