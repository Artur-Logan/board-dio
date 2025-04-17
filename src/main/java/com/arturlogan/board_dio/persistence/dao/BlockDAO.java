package com.arturlogan.board_dio.persistence.dao;

import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static com.arturlogan.board_dio.persistence.converter.OffsetDateTimeConverter.toTimestamp;

@AllArgsConstructor
public class BlockDAO {

    private final Connection connection;

    public void block(final String reason, final Long cardId) throws SQLException {
        var sql = "INSERT INTO BLOCKS (block_at, block_reason, card_id) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setTimestamp(i ++, toTimestamp(OffsetDateTime.now()));
            statement.setString(i ++, reason);
            statement.setLong(i, cardId);
            statement.executeUpdate();
            }
        }

    public void unblock(String reason, Long id) throws SQLException{

        var sql = "UPDATE BLOCKS\n" +
                "SET unblock_at = ?,\n" +
                "    unblock_reason = ?\n" +
                "WHERE card_id = ?\n" +
                "  AND unblock_at IS NULL";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setTimestamp(i ++, toTimestamp(OffsetDateTime.now()));
            statement.setString(i ++, reason);
            statement.setLong(i, id);
            statement.executeUpdate();
        }
    }
}
