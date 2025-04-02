package com.arturlogan.board_dio.persistence.dao;

import com.arturlogan.board_dio.dto.CardDetails;
import lombok.AllArgsConstructor;

import java.sql.Connection;

@AllArgsConstructor
public class CardDAO {

    private final Connection connection;

    public CardDetails findById(final Long id){

        return null;
    }
}
