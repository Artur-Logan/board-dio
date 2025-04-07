package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.dto.BoardColumnDTO;
import com.arturlogan.board_dio.dto.CardDetailsDTO;
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
import static java.util.Objects.isNull;

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
        var sql = """
        SELECT
            c.id as card_id,
            c.title as card_title,
            c.description as card_description,
            bc.name as column_name,
            bc.kind as column_kind,
            bc.id as column_id,
            COUNT(b.id) as block_count
        FROM CARDS c
        JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id
        LEFT JOIN BLOCKS b ON c.id = b.card_id AND b.unblock_at IS NULL
        WHERE bc.board_id = ?
        GROUP BY c.id, bc.id
        """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (var resultSet = statement.executeQuery()) {
                List<BoardColumnDTO> results = new ArrayList<>();
                while (resultSet.next()) {
                    var dto = new BoardColumnDTO(
                            resultSet.getLong("column_id"), // Correção aqui!
                            resultSet.getString("column_name"), // Correção aqui!
                            findByName(resultSet.getString("column_kind")), // Correção aqui!
                            resultSet.getInt("block_count")
                    );

                    results.add(dto);
                }
                return results;
            }
        }
    }
    public Optional<BoardColumnEntity> findById(Long boardId) throws SQLException {
        var sql = "SELECT bc.id, bc.name, bc.kind, c.id, c.title, c.description FROM BOARDS_COLUMNS bc LEFT JOIN CARDS c ON c.board_column_id = bc.id WHERE bc.id = ?";

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    var entity = new BoardColumnEntity();
                    entity.setId(resultSet.getLong("bc.id"));
                    entity.setName(resultSet.getString("bc.name"));
                    entity.setKind(findByName(resultSet.getString("bc.kind")));
                    do {
                        if (isNull(resultSet.getString("c.title"))) {
                            continue;
                        }
                        var card = new CardEntity();
                        card.setId(resultSet.getLong("c.id"));
                        card.setTitle(resultSet.getString("c.title"));
                        card.setDescription(resultSet.getString("c.description"));
                        entity.getCards().add(card);
                    } while (resultSet.next());
                    return Optional.of(entity);
                }
                return Optional.empty();
            }
        }
    }
}
