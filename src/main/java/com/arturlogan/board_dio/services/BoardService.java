package com.arturlogan.board_dio.services;

import com.arturlogan.board_dio.persistence.dao.BoardColumnDAO;
import com.arturlogan.board_dio.persistence.dao.BoardDAO;
import com.arturlogan.board_dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class BoardService {

    private Connection connection;

    public boolean delete(final Long id) throws SQLException{
        var dao = new BoardDAO(connection);
        try{
            if (!dao.exists(id)){
               return false;
            }
            dao.delete(id);
            connection.commit();
            return true;
        } catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public BoardEntity insert(final BoardEntity board)throws SQLException{
        var dao = new BoardDAO(connection);
        var boardColumn = new BoardColumnDAO(connection);
        try{
            dao.insert(board);
           var columns = board.getBoardColumns().stream().map(c -> {
                c.setBoard(board);
                return c;
            }).toList();

           for (var column : columns ){
               boardColumn.insert(column);
           }
            connection.commit();
        } catch (SQLException e){
            connection.rollback();
            throw e;
        }

        return board;
    }
}
