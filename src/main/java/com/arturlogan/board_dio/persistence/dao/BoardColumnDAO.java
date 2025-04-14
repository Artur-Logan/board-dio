package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.dto.BoardColumnDTO;
import com.arturlogan.board_dio.dto.CardDetailsDTO;
import com.arturlogan.board_dio.persistence.entity.BoardColumnEntity;
import com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum;
import com.arturlogan.board_dio.persistence.entity.BoardEntity;
import com.arturlogan.board_dio.persistence.entity.CardEntity;
import lombok.RequiredArgsConstructor;

import java.sql.*;
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
                    bc.id as column_id,
                    bc.name as column_name,
                    bc.kind as column_kind,
                    COUNT(c.id) as cards_amount
                FROM BOARDS_COLUMNS bc
                LEFT JOIN CARDS c ON bc.id = c.board_column_id
                WHERE bc.board_id = ?
                GROUP BY bc.id, bc.name, bc.kind
                ORDER BY bc.id;
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (var resultSet = statement.executeQuery()) {
                List<BoardColumnDTO> results = new ArrayList<>();
                System.out.println("DEBUG: findByBoardIdWithDetails - Iniciando processamento do ResultSet");
                while (resultSet.next()) {
                    var dto = new BoardColumnDTO(
                            resultSet.getLong("column_id"),
                            resultSet.getString("column_name"),
                            findByName(resultSet.getString("column_kind")),
                            resultSet.getInt("cards_amount")
                    );
                    System.out.printf("DEBUG: findByBoardIdWithDetails - Coluna lida: %s (%s) com %s cards\n", dto.name(), dto.id(), dto.cardsAmount());
                    results.add(dto);
                }
                System.out.println("DEBUG: findByBoardIdWithDetails - Número total de colunas lidas: " + results.size());
                return results;
            }
        }
    }

    public Optional<BoardColumnEntity> findById(final Long boardColumnId) throws SQLException {
        var sql = """
                    SELECT
                        bc.name AS column_name,
                        bc.kind AS column_kind,
                        c.id AS card_id,
                        c.title AS card_title,
                        c.description AS card_description
                    FROM BOARDS_COLUMNS bc
                    LEFT JOIN CARDS c ON c.board_column_id = bc.id
                    WHERE bc.id = ?;
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardColumnId);
            try (ResultSet resultSet = statement.executeQuery()) {
                BoardColumnEntity entity = null;
                while (resultSet.next()) {
                    if (entity == null) {
                        entity = new BoardColumnEntity();
                        entity.setId(boardColumnId); // Usando o ID passado como parâmetro
                        entity.setName(resultSet.getString("column_name"));
                        entity.setKind(findByName(resultSet.getString("column_kind")));
                        entity.setCards(new ArrayList<>());
                    }

                    if (!isNull(resultSet.getString("card_title"))) {
                        var card = new CardEntity();
                        card.setId(resultSet.getLong("card_id"));
                        card.setTitle(resultSet.getString("card_title"));
                        card.setDescription(resultSet.getString("card_description"));
                        entity.getCards().add(card);
                    }
                }
                return Optional.ofNullable(entity);
            }
        }
    }
}
