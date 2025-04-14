package com.arturlogan.board_dio.services;

import com.arturlogan.board_dio.dto.BoardDetailsDTO;
import com.arturlogan.board_dio.persistence.dao.BoardColumnDAO;
import com.arturlogan.board_dio.persistence.dao.BoardDAO;
import com.arturlogan.board_dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardQueryService {

    private Connection connection;

    public Optional<BoardEntity>  findById(final Long id) throws SQLException{
        var dao = new BoardDAO(connection);
        var boardColumnDao = new BoardColumnDAO(connection);
        var optional = dao.findById(id);

        if (optional.isPresent()){
            var entity = optional.get();
            entity.setBoardColumns(boardColumnDao.findByBoardId(entity.getId()));
            return Optional.of(entity);
        }

        return Optional.empty();
    }

    public Optional<BoardDetailsDTO> showBoardDetails(final Long id)  throws SQLException{
        var dao = new BoardDAO(connection);
        var boardColumnDao = new BoardColumnDAO(connection);
        var optional = dao.findById(id);

        if (optional.isPresent()){
            var entity = optional.get();
            var columns = boardColumnDao.findByBoardIdWithDetails(entity.getId());
            System.out.println("DEBUG: showBoardDetails - Número de colunas encontradas: " + columns.size());columns.forEach(col -> System.out.printf("DEBUG: showBoardDetails - Coluna: %s (%s) com %s cards\n", col.name(), col.id(), col.cardsAmount()));
            var dto = new BoardDetailsDTO(entity.getId(), entity.getName(), columns);
            return Optional.of(dto);
        }
        return Optional.empty();
    }
}
