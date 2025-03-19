package com.arturlogan.board_dio.persistence.dao;

import java.sql.Connection;


public class BoardDAO {

    private final Connection connection;


    public BoardDAO(Connection connection) {
        this.connection = connection;
    }


}
