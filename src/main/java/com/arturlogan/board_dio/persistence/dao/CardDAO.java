package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.dto.CardDetailsDTO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static com.arturlogan.board_dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;

@AllArgsConstructor
public class CardDAO {

    private final Connection connection;

    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        var sql =
                """
                        SELECT c.id,
                               c.name,
                               c.title,
                               c.description,
                               b.block_at,
                               b.block_reason,
                               c.board_column_id,
                               bc.name,
                               COUNT(SELECT sub_b.id FROM BLOCKS sub_b WHERE sub_b.card_id = c.id) blocks_amount
                               FROM CARDS c
                               LEFT JOIN BLOCKS b
                                  ON c.id = b.card_id
                               AND b.unblock_at IS NULL
                               INNER JOIN BOARDS_COLUMNS bc
                                  ON bc.id = c.board_column_id
                               WHERE id = ?;
                        """;
        try (var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();

            var resultSet = statement.getResultSet();
            if (resultSet.next()){
                var dto = new CardDetailsDTO(
                        resultSet.getLong("c.id"),
                        resultSet.getString("c.title"),
                        resultSet.getString("c.description"),
                        resultSet.getString("c.block_reason").isEmpty(),
                        toOffsetDateTime(resultSet.getTimestamp("b.block_at")),
                        resultSet.getString("c.block_reason"),
                        resultSet.getInt("b.blocks_amount"),
                        resultSet.getLong("c.board_column_id"),
                        resultSet.getString("bc.name")
                );
            }
        }
        return null;
    }
}
