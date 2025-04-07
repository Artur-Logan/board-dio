package com.arturlogan.board_dio.services;

import com.arturlogan.board_dio.persistence.dao.CardDAO;
import com.arturlogan.board_dio.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity insert(final CardEntity  entity) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }
}
