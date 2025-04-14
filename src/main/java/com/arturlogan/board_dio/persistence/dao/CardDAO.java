package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.dto.CardDetailsDTO;
import com.arturlogan.board_dio.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.*;
import java.util.Optional;

import static com.arturlogan.board_dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class CardDAO {

    private final Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException{
        var sql = "INSERT INTO CARDS (title, description, board_column_id) VALUES (?, ?, ?);";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            var i = 1;
            preparedStatement.setString(i ++, entity.getTitle());
            preparedStatement.setString(i ++, entity.getDescription());
            preparedStatement.setLong(i, entity.getBoardColumn().getId());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Falha ao obter o ID gerado.");
                }
            }
        }
        return entity;
    }

    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        // ... (seu código findById permanece o mesmo) ...
        var sql =
                """
                        SELECT c.id AS card_id,
                               c.title AS card_title,
                               c.description AS card_description,
                               b.block_at AS block_at,
                               b.block_reason AS block_reason,
                               c.board_column_id AS board_column_id,
                               bc.name AS column_name,
                               (SELECT COUNT(sub_b.id)
                                  FROM BLOCKS sub_b
                                 WHERE sub_b.card_id = c.id) AS blocks_amount
                          FROM CARDS c
                          LEFT JOIN BLOCKS b
                             ON c.id = b.card_id
                            AND b.unblock_at IS NULL
                         INNER JOIN BOARDS_COLUMNS bc
                            ON bc.id = c.board_column_id
                         WHERE c.id = ?;
                        """;
        try (var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            try (var resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    var dto = new CardDetailsDTO(
                            resultSet.getLong("card_id"),
                            resultSet.getString("card_title"),
                            resultSet.getString("card_description"),
                            nonNull(resultSet.getString("block_reason")),
                            toOffsetDateTime(resultSet.getTimestamp("block_at")),
                            resultSet.getString("block_reason"),
                            resultSet.getInt("blocks_amount"),
                            resultSet.getLong("board_column_id"),
                            resultSet.getString("column_name"));
                    return Optional.of(dto);
                }
            }
        }
        return Optional.empty();
    }

    public void movetoColumn(final Long columnId, final Long cardId) throws SQLException{
        var sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?;";

        try (var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setLong(i ++, columnId);
            statement.setLong(i ++, cardId);
            int rowsUpdated = statement.executeUpdate();
            System.out.println("DEBUG (CardDAO.movetoColumn): Rows updated = " + rowsUpdated);
            if (rowsUpdated == 0) {
                System.out.println("DEBUG (CardDAO.movetoColumn): Nenhum card foi atualizado para cardId: " + cardId);
            }
        }
    }
}