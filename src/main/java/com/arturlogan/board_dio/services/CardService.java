package com.arturlogan.board_dio.services;

import com.arturlogan.board_dio.dto.BoardColumnInfoDTO;
import com.arturlogan.board_dio.dto.CardDetailsDTO;
import com.arturlogan.board_dio.exception.CardBlockedException;
import com.arturlogan.board_dio.exception.CardFinishedException;
import com.arturlogan.board_dio.exception.EntityNotFoundException;
import com.arturlogan.board_dio.persistence.dao.CardDAO;
import com.arturlogan.board_dio.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.arturlogan.board_dio.persistence.config.ConnectionConfig.getConnection;
import static com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var cardDAO = new CardDAO(connection);
            var optionalCardDetails = cardDAO.findById(cardId);
            var cardDetails = optionalCardDetails.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );

            if (cardDetails.blocked()) {
                throw new CardBlockedException("O card %s está bloqueado, é necessário desbloquea-lo para mover".formatted(cardId));
            }

            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(cardDetails.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("A coluna atual do card não foi encontrada no board."));

            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }

            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card esta cancelado."));

            cardDAO.movetoColumn(nextColumn.id(), cardId);
            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } catch (EntityNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CardBlockedException e) {
            throw new RuntimeException(e);
        } catch (CardFinishedException e) {
            throw new RuntimeException(e);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    public void cancel(final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try{
            var dao = new CardDAO(this.connection); // Use a conexão injetada
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.blocked()) {
                throw new CardBlockedException("O card %s está bloqueado, é necessário desbloquea-lo para mover".formatted(cardId));
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("A coluna atual do card não foi encontrada no board."));

            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }

            dao.movetoColumn(cancelColumnId, cardId);
            this.connection.commit(); // Use a conexão injetada
        } catch (SQLException ex){
            this.connection.rollback(); // Use a conexão injetada
            throw ex;
        }
    }
}